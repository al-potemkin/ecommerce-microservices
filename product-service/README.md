# product-service

Микросервис каталога товаров учебного ecommerce-проекта (магазин парфюмерии). Хранит продукты и их варианты (объёмы),
отдаёт публичный каталог с фильтрами и фасетами, публикует события об изменениях товаров в Kafka и принимает события о
наличии от inventory-service.

Stack: Java 21, Spring Boot 4, Spring Security OAuth2 Resource Server, Spring Data MongoDB, MongoDB, Redis, Spring
Cache, Apache Kafka, Avro, Schema Registry, Keycloak, Lombok, MapStruct, Actuator

## Архитектура

```
Клиент ──► api-gateway ──► product-service ──► MongoDB (продукты, outbox)
                                │
                                ├──► Redis (кэш карточки и фильтров)
                                ├──► Kafka: product-events  (публикует)
                                └──◄ Kafka: inventory-events (потребляет)

order-service / inventory-service ──► /internal/** (service token)
```

- Наружу через gateway доступны только `/api/**`. Маршрут `/internal/**` через gateway не публикуется.
- Product-service ходит только в Keycloak (ключи подписи JWT), MongoDB, Redis и Kafka.
- События пишутся не напрямую в Kafka, а в коллекцию `outbox` в той же транзакции Mongo, что и изменение данных. Фоновый
  relay публикует их в Kafka.

## Доменные правила

- Новый продукт создаётся **неактивным** (`active=false`).
- Вариант (`Variant`): `sku`, `volumeMl`, `price`, `active` (по умолчанию `true`), `inStock` (по умолчанию `false`).
- **Видимость для покупателей:** `product.active && variant.active`. Неактивные продукты и варианты не отдаются в
  каталоге и по id (для менеджера `GET /api/products/{id}` возвращает и неактивные).
- **`inStock`** только информационный флаг: продукт с `active=true` и `inStock=false` остаётся в каталоге и доступен по
  id. Владелец остатков inventory-service; флаг обновляется событиями. В каталоге есть фильтр `inStock=true` и фасет
  `inStockCount`.
- Если у продукта не осталось активных вариантов, продукт автоматически деактивируется (событие `PRODUCT_DEACTIVATED`).
- Активация продукта без активных вариантов отклоняется: `409 NO_ACTIVE_VARIANTS`.
- Неактивные продукты могут получать остатки на складе.
- Конкурентные изменения защищены оптимистичной блокировкой (`@Version`), при конфликте `409 CONCURRENT_MODIFICATION`.
- SKU уникален: дубликат даёт `409 SKU_ALREADY_EXISTS`.
- Цены хранятся как `Decimal128`.

## API

### Публичный каталог (без токена)

| Метод | Путь                    | Описание                                                                                                                                                        |
|-------|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GET   | `/api/products`         | Список активных продуктов с фильтрами, сортировкой и пагинацией (параметры ниже)                                                                                |
| GET   | `/api/products/filters` | Фасеты: бренды, пол, ароматы, объёмы, диапазон цен, `inStockCount`. Список значений («universe») берётся из кэша, количества пересчитываются под текущий фильтр |
| GET   | `/api/products/{id}`    | Карточка продукта. Менеджеру доступны и неактивные, остальным только активные продукты с хотя бы одним активным вариантом                                       |

Диапазон цен в фасетах округляется наружу (минимум вниз, максимум вверх до целых).

**Параметры `GET /api/products` и `GET /api/products/filters`** (`CatalogFilter`):

| Параметр               | Тип                 | Описание                                               |
|------------------------|---------------------|--------------------------------------------------------|
| `brand`                | набор строк         | Бренды (можно несколько значений)                      |
| `gender`               | набор `Gender`      | Пол                                                    |
| `scent`                | набор `ScentFamily` | Семейства ароматов                                     |
| `volume`               | набор чисел         | Объёмы в мл                                            |
| `priceMin`, `priceMax` | число `>= 0`        | Диапазон цены вариантов                                |
| `inStock`              | boolean             | `true` оставляет только то, что есть в наличии         |
| `page`                 | число `>= 0`        | Номер страницы, по умолчанию `0` (только для списка)   |
| `size`                 | `1..100`            | Размер страницы, по умолчанию `20` (только для списка) |
| `sort`                 | `CatalogSort`       | Сортировка, по умолчанию `NEWEST` (только для списка)  |

### Управление каталогом (роль `PRODUCT_MANAGER`)

| Метод  | Путь                                       | Описание                                                                                   | Ответ                               |
|--------|--------------------------------------------|--------------------------------------------------------------------------------------------|-------------------------------------|
| POST   | `/api/products`                            | Создать продукт (создаётся неактивным)                                                     | 201, `Location: /api/products/{id}` |
| PUT    | `/api/products/{productId}`                | Обновить данные продукта                                                                   | 200                                 |
| DELETE | `/api/products/{productId}`                | Деактивировать продукт (не физическое удаление)                                            | 204                                 |
| POST   | `/api/products/{productId}/activate`       | Активировать продукт (нужен хотя бы один активный вариант, иначе `409 NO_ACTIVE_VARIANTS`) | 200                                 |
| POST   | `/api/products/{productId}/variants`       | Добавить вариант                                                                           | 201                                 |
| PATCH  | `/api/products/{productId}/variants/{sku}` | Изменить вариант (цена, активность)                                                        | 200                                 |

### Модель данных

Продукт (коллекция `products`):

| Поле                           | Описание                                                   |
|--------------------------------|------------------------------------------------------------|
| `id`                           | Идентификатор                                              |
| `name`, `brand`, `description` | Название, бренд, описание                                  |
| `gender`                       | Пол (`Gender`)                                             |
| `scentFamilies`                | Семейства ароматов (`Set<ScentFamily>`)                    |
| `mainImage`, `gallery`         | Главное изображение и галерея                              |
| `variants`                     | Варианты (`sku`, `volumeMl`, `price`, `active`, `inStock`) |
| `active`                       | Виден ли продукт покупателям                               |
| `createdAt`, `updatedAt`       | Аудит                                                      |
| `version`                      | Версия для оптимистичной блокировки                        |

Составные индексы: `active + createdAt + _id`, `active + brand`, `active + gender`, `active + scentFamilies`,
`variants.volumeMl + variants.price`.

### Внутренний API (роль `INTERNAL_SERVICE`, service token)

| Метод | Путь                             | Описание                                                                                                                                                                            |
|-------|----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GET   | `/internal/variants?skus=A,B,C`  | Актуальные данные по SKU (до 100 штук): `sku, productId, name, volumeMl, price, active, inStock`. `active = product.active && variant.active`. Неизвестные SKU в ответе отсутствуют |
| PUT   | `/internal/variants/{sku}/stock` | Тело `{"inStock": true}`. Идемпотентно, ответ 204                                                                                                                                   |

## Безопасность

- Сервис проверяет JWT от Keycloak (`issuer-uri` для проверки `iss`, `jwk-set-uri` на внутренний адрес Keycloak в сети
  compose).
- Роли берутся из `realm_access.roles` и получают префикс `ROLE_`.

| Доступ                                                             | Правило                                                  |
|--------------------------------------------------------------------|----------------------------------------------------------|
| `GET /api/products`, `/api/products/filters`, `/api/products/{id}` | публично                                                 |
| `/api/products/**` (остальные методы)                              | `PRODUCT_MANAGER`                                        |
| `/internal/**`                                                     | `INTERNAL_SERVICE` (service account, client credentials) |
| `/actuator/health/**`                                              | публично                                                 |
| всё остальное                                                      | аутентифицированный пользователь                         |

Ответы 401 и 403 приходят в том же формате, что и остальные ошибки.

## Формат ошибок

Все ошибки возвращаются в формате RFC 9457 (`application/problem+json`) через `ApiProblem`. В ответе есть код ошибки (
`ErrorCode`), `traceId` запроса и, для валидации, список полей с ошибками.

Основные коды: `VALIDATION_FAILED`, `UNAUTHORIZED`, `FORBIDDEN`, `SKU_ALREADY_EXISTS`, `NO_ACTIVE_VARIANTS`,
`CONCURRENT_MODIFICATION`, `SERVICE_UNAVAILABLE`, `INTERNAL_ERROR`.

**Логирование.** Ошибки логируются в одной точке, в `GlobalExceptionHandler`:

- 5xx: `ERROR` со стеком;
- 4xx: `WARN` одной строкой (метод, путь, статус, класс исключения).

`traceId` из ответа можно найти в строках лога.

## Кэширование

Redis через Spring Cache, префикс ключей `product-service:`, TTL 30 минут как страховка.

| Кэш               | Что хранит                                       | Когда сбрасывается                                           |
|-------------------|--------------------------------------------------|--------------------------------------------------------------|
| `PRODUCT_CARD`    | Карточка продукта для не-менеджеров              | Любое изменение продукта или варианта и обновление `inStock` |
| `FILTER_UNIVERSE` | Полный список значений фильтров (бренды, объёмы) | При изменениях, влияющих на каталог                          |

Сброс происходит после коммита транзакции. Недоступный Redis не ломает сервис: ошибки кэша логируются, запросы идут в
MongoDB.

## События Kafka

### Публикуемые: топик `product-events`

- 3 партиции, ключ сообщения `productId` (порядок событий одного продукта сохраняется), формат Avro, схема
  `product-event.avsc`, совместимость BACKWARD.
- Доставка **at least once**: потребители должны быть идемпотентными.

| Событие               | Когда отправляется                                                               |
|-----------------------|----------------------------------------------------------------------------------|
| `PRODUCT_CREATED`     | Создан продукт                                                                   |
| `PRODUCT_ACTIVATED`   | Продукт реально стал активным                                                    |
| `PRODUCT_DEACTIVATED` | Продукт деактивирован вручную или автоматически (не осталось активных вариантов) |
| `VARIANT_ADDED`       | Добавлен вариант                                                                 |
| `VARIANT_ACTIVATED`   | Вариант стал активным                                                            |
| `VARIANT_DEACTIVATED` | Вариант стал неактивным                                                          |

Поля: `eventId`, `type`, `occurredAt`, `productId`, `name`, `productActive`, `variants[] { sku, volumeMl, active }`.
Каждое событие несёт актуальный снимок вариантов продукта. Неизвестное значение `type` десериализуется как `UNKNOWN`.

### Потребляемые: топик `inventory-events`

- Группа потребителей `product-service`, ключ сообщения `sku`, формат Avro (`inventory-event.avsc`).

| Событие            | Действие                              |
|--------------------|---------------------------------------|
| `SKU_IN_STOCK`     | `inStock=true` у варианта с этим SKU  |
| `SKU_OUT_OF_STOCK` | `inStock=false`                       |
| `UNKNOWN`          | Пропускается с предупреждением в логе |

События несут состояние, а не приращение, поэтому повторная доставка безвредна. Неизвестный SKU не повторяется (
логируется как ошибка), остальные ошибки повторяются 5 раз с паузой 1 секунда.

### Transactional outbox

1. Изменение продукта и запись в `outbox` происходят в одной транзакции MongoDB (поэтому нужен replica set).
2. `OutboxRelay` каждую секунду (`app.outbox.poll-delay-ms`) берёт `PENDING`-записи по порядку, преобразует JSON-payload
   в Avro и отправляет в Kafka. При ошибке пачка останавливается на первом сбое, чтобы сохранить порядок.
3. Успешные записи помечаются `SENT` и удаляются по TTL через 7 дней.

## Конфигурация

Настройки задаются переменными окружения (в compose они уже заданы).

| Переменная                                                  | Назначение                                                                   |
|-------------------------------------------------------------|------------------------------------------------------------------------------|
| `SERVER_PORT`                                               | Порт сервиса (`8081`)                                                        |
| `MONGODB_HOST`, `MONGODB_PORT`                              | Адрес MongoDB                                                                |
| `PRODUCT_DB_NAME`, `PRODUCT_DB_USER`, `PRODUCT_DB_PASSWORD` | База и пользователь приложения                                               |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`                | Redis                                                                        |
| `JWT_ISSUER_URI`                                            | Значение `iss` в токенах Keycloak (`http://localhost:8180/realms/ecommerce`) |
| `JWT_JWK_SET_URI`                                           | Адрес ключей Keycloak внутри сети Docker                                     |
| `KAFKA_BOOTSTRAP_SERVERS`                                   | Брокер Kafka (`kafka:9092`)                                                  |
| `SCHEMA_REGISTRY_URL`                                       | Schema Registry (`http://schema-registry:8081`)                              |
| `SPRING_PROFILES_ACTIVE`                                    | `dev` включает OpenAPI и детали health. Без профиля режим прод-подобный      |

Секреты хранятся в корневом файле `.env` проекта (в репозиторий не коммитится).

## Запуск

### Всё в Docker

Из корня проекта:

```
docker compose up -d --build
docker compose ps
```

Обычный запуск подхватывает `docker-compose.override.yaml` (dev-доступ с хоста). Прод-подобный запуск без публикации
портов баз и брокера:

```
docker compose -f docker-compose.yaml up -d --build
```

### Dev-порты на хосте (только `127.0.0.1`)

| Сервис          | Адрес                   |
|-----------------|-------------------------|
| api-gateway     | `http://127.0.0.1:8080` |
| product-service | `http://127.0.0.1:8081` |
| Keycloak        | `http://127.0.0.1:8180` |
| Kafka UI        | `http://127.0.0.1:8082` |
| Schema Registry | `http://127.0.0.1:8083` |
| MongoDB         | `127.0.0.1:27017`       |
| Redis (product) | `127.0.0.1:6380`        |
| Kafka (для IDE) | `127.0.0.1:29092`       |

Используй `127.0.0.1`, а не `localhost`: на Windows `localhost` сначала резолвится в IPv6 и добавляет ~200 мс.

### Подключение к MongoDB с хоста

Replica set объявлен с адресом `product-mongo:27017`, поэтому клиенту с хоста нужен либо `directConnection=true`:

```
mongodb://<user>:<password>@127.0.0.1:27017/<db>?directConnection=true&authSource=<db>
```

либо запись `127.0.0.1 product-mongo` в файле `hosts`.

### Сборка

```
mvn clean package
```

При сборке `avro-maven-plugin` генерирует Java-классы из `src/main/avro/*.avsc`.

### Первый запуск

Схема `inventory-events-value` пока регистрируется вручную (inventory-service ещё не существует) через
`POST http://127.0.0.1:8083/subjects/inventory-events-value/versions`.

## Наблюдаемость

- **Health:** `/actuator/health`, а для compose `/actuator/health/readiness` (включает Mongo, Redis не включает: его
  недоступность не делает сервис нездоровым). В dev показываются детали.
- **Трассировка:** Micrometer Tracing кладёт `traceId` и `spanId` в MDC, поэтому они есть в строках лога и в теле
  ошибки. Экспорт во внешние системы пока выключен.
- **Kafka UI** позволяет смотреть топики, сообщения (Avro расшифровывается через Schema Registry) и группы потребителей.

## Планы и ограничения

- Централизованные логи через ELK.
- Dead Letter Topic для consumer склада (сейчас после повторов сообщение пропускается).
- ShedLock или аренда для `OutboxRelay` при нескольких экземплярах сервиса.
- Топик и схема `inventory-events` перейдут во владение inventory-service.
- Интеграционные тесты (Testcontainers: Mongo replica set, Kafka, Schema Registry).
- `auto.register.schemas` включён только для dev, в проде схемы нужно регистрировать осознанно.