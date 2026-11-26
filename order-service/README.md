# order-service

Микросервис заказов учебного ecommerce-проекта (магазин парфюмерии). Принимает заказы от покупателей, фиксирует цены из
каталога, резервирует остатки на складе, обрабатывает результат оплаты, истечение резерва, отмену и возврат, даёт
менеджеру обработку заказов и публикует события о заказах в Kafka.

Stack: Java 21, Spring Boot 4, Spring Security OAuth2 Resource Server и OAuth2 Client (client credentials), Spring Data
JPA, PostgreSQL, Flyway, Apache Kafka, Avro, Schema Registry, Keycloak, Lombok, MapStruct, Actuator

## Архитектура

```
Клиент ──► api-gateway ──► order-service ──► PostgreSQL (заказы, outbox)
                                │
                                ├──► product-service   GET /internal/variants        (цены, активность)
                                ├──► inventory-service POST /internal/reservations/** (reserve / commit / release)
                                ├──► Kafka: order-events        (публикует)
                                ├──◄ Kafka: payment-events      (потребляет)
                                └──◄ Kafka: reservation-events  (потребляет)

inventory-service ──◄ Kafka: order-events (ORDER_RETURNED → возврат остатка на склад)
```

- Наружу через gateway доступны только `/api/orders/**`.
- Вызовы в другие сервисы идут по внутренней сети со service token (client credentials, клиент `order-service` в
  Keycloak, роль `INTERNAL_SERVICE`).
- События пишутся не напрямую в Kafka, а в таблицу `outbox` в той же транзакции PostgreSQL, что и смена статуса заказа.
  Фоновый relay публикует их в Kafka.

## Жизненный цикл заказа

Статусы: `AWAITING_PAYMENT`, `PAID`, `SHIPPED`, `RETURN_REQUESTED`, `RETURNED`, `CANCELLED`, `EXPIRED`.

| Переход                                   | Кто запускает                                       |
|-------------------------------------------|-----------------------------------------------------|
| создание → `AWAITING_PAYMENT`             | покупатель (`POST /api/orders`)                     |
| `AWAITING_PAYMENT` → `PAID`               | событие `PAYMENT_SUCCEEDED`                         |
| `AWAITING_PAYMENT` → `CANCELLED`          | покупатель (`/cancel`) или событие `PAYMENT_FAILED` |
| `AWAITING_PAYMENT` → `EXPIRED`            | событие `RESERVATION_EXPIRED` из inventory          |
| `PAID` → `SHIPPED`                        | менеджер (`/ship`)                                  |
| `PAID` или `SHIPPED` → `RETURN_REQUESTED` | покупатель (`/return`)                              |
| `RETURN_REQUESTED` → `RETURNED`           | менеджер (`/return/confirm`)                        |

Допустимые переходы заданы в одном месте, `OrderStatus.allowedTransitions()`. Любая смена статуса идёт через
`OrderService.transition(...)`, он же проставляет время перехода и пишет событие в outbox. Недопустимый переход даёт
`409 INVALID_ORDER_TRANSITION`.

## Доменные правила

- **Цены и названия** клиент не присылает. Они берутся из product-service и фиксируются в позициях заказа (снимок на
  момент заказа). Валюта одна: гривна (UAH).
- **Создание заказа** (`POST /api/orders`):
    1. по `Idempotency-Key` и `userId` считается детерминированный `orderId` (UUID); повтор запроса с тем же ключом
       возвращает уже созданный заказ;
    2. product-service: проверка, что все SKU существуют и активны (иначе `409 ITEMS_UNAVAILABLE` с `errors[]` по SKU);
    3. inventory-service: резерв по `orderId` (нехватка даёт `409 INSUFFICIENT_STOCK`);
    4. заказ сохраняется в `AWAITING_PAYMENT`, срок оплаты `expiresAt` берётся из ответа inventory.
- Создание не выполняется в одной транзакции БД: HTTP-вызовы не должны держать соединение. Если сохранение упадёт после
  резерва, резерв сам истечёт по TTL в inventory. Параллельные запросы с одним ключом разруливает первичный ключ.
- **Таймер резерва живёт в inventory-service** (TTL настраивается там). По истечении inventory освобождает резерв и
  публикует `RESERVATION_EXPIRED`, заказ переходит в `EXPIRED`. Собственного планировщика истечения в order-service нет.
- **Оплата**: при успехе сначала `commit` резерва в inventory, затем `PAID`; при неуспехе `release` и `CANCELLED`.
  Дубликаты событий оплаты игнорируются. Если оплата пришла, а резерв уже освобождён (`RESERVATION_RELEASED`), ошибка
  уходит в DLT и логируется как `ERROR` (требуется ручной возврат денег).
- **Отмена** покупателем возможна только до оплаты. Повторная отмена возвращает заказ без ошибки.
- **Возврат**: запрос возможен из `PAID` без ограничений по сроку и из `SHIPPED` в течение
  `app.order.return-window-days` (14) дней после отправки (`shippedAt`), иначе `409 RETURN_WINDOW_EXPIRED`. При
  подтверждении возврата inventory возвращает остаток на склад.
- Чужой заказ покупателю не виден: `404 ORDER_NOT_FOUND` (и в списке, и по id).
- Конкурентные изменения заказа защищены оптимистичной блокировкой (`@Version`), при конфликте
  `409 CONCURRENT_MODIFICATION`.
- Повтор запроса создания с тем же `Idempotency-Key` после истечения резерва даёт `409 RESERVATION_RELEASED`: нужен
  новый ключ.

## API

### Покупатель (любой аутентифицированный пользователь)

| Метод | Путь                      | Описание                                                                                                   | Ответ |
|-------|---------------------------|------------------------------------------------------------------------------------------------------------|-------|
| POST  | `/api/orders`             | Создать заказ. Заголовок `Idempotency-Key` (до 64 символов) обязателен                                     | 201   |
| GET   | `/api/orders`             | Свои заказы, новые сверху. Параметры `page` (по умолчанию 0), `size` (1..50, по умолчанию 20). Без позиций | 200   |
| GET   | `/api/orders/{id}`        | Свой заказ с позициями                                                                                     | 200   |
| POST  | `/api/orders/{id}/cancel` | Отменить до оплаты                                                                                         | 200   |
| POST  | `/api/orders/{id}/return` | Запросить возврат                                                                                          | 200   |

Тело `POST /api/orders`:

```json
{
  "items": [{"sku": "SKU-1", "quantity": 2}],
  "delivery": {
    "recipientName": "Иван Петренко",
    "recipientPhone": "+380501234567",
    "city": "Харків",
    "address": "вул. Сумська, 1"
  }
}
```

Валидация: `items` от 1 до 50 позиций, `quantity` 1..1000, телефон в формате `+380XXXXXXXXX`. Одинаковые SKU в позициях
суммируются. В ответе у позиции есть `lineTotal` (`unitPrice × quantity`).

### Менеджер (роль `ORDER_MANAGER`)

| Метод | Путь                                     | Описание                                                       |
|-------|------------------------------------------|----------------------------------------------------------------|
| GET   | `/api/orders/manage`                     | Все заказы, фильтр `status`, пагинация как у списка покупателя |
| GET   | `/api/orders/manage/{id}`                | Любой заказ с позициями                                        |
| POST  | `/api/orders/manage/{id}/ship`           | `PAID` → `SHIPPED`                                             |
| POST  | `/api/orders/manage/{id}/return/confirm` | `RETURN_REQUESTED` → `RETURNED`                                |

### Только для разработки (профиль `dev`, роль `ORDER_MANAGER`)

| Метод | Путь                                                    | Описание                                                                                          |
|-------|---------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| POST  | `/api/orders/dev/{orderId}/payment?success=true\|false` | Имитирует событие оплаты: публикует `PaymentEvent` в `payment-events`. Заменяется payment-service |

### Модель данных

Заказ (таблица `orders`):

| Поле                                                                     | Описание                                                                          |
|--------------------------------------------------------------------------|-----------------------------------------------------------------------------------|
| `id`                                                                     | UUID, задаётся приложением (детерминированно из пользователя и `Idempotency-Key`) |
| `user_id`                                                                | `sub` из токена Keycloak                                                          |
| `status`                                                                 | Статус заказа                                                                     |
| `total_amount`                                                           | Итоговая сумма, UAH                                                               |
| `recipient_name`, `recipient_phone`, `delivery_city`, `delivery_address` | Снимок данных доставки                                                            |
| `expires_at`                                                             | Срок оплаты (из резерва inventory)                                                |
| `paid_at`, `shipped_at`, `return_requested_at`, `finished_at`            | Время переходов (`finished_at` для `RETURNED`, `CANCELLED`, `EXPIRED`)            |
| `version`, `created_at`, `updated_at`                                    | Версия блокировки и аудит                                                         |

Позиция (таблица `order_item`): `sku`, `product_id`, `product_name`, `volume_ml`, `unit_price`, `quantity` (снимок на
момент заказа), уникальность `(order_id, sku)`.

Индексы: `(user_id, created_at DESC)`, `(status, created_at DESC)`. Схемой управляет Flyway, Hibernate только проверяет
её (`ddl-auto: validate`).

## Безопасность

- Сервис проверяет JWT от Keycloak (`issuer-uri` для проверки `iss`, `jwk-set-uri` на внутренний адрес Keycloak в сети
  compose).
- Роли берутся из `realm_access.roles` и получают префикс `ROLE_`.

| Доступ                  | Правило                                               |
|-------------------------|-------------------------------------------------------|
| `/actuator/health/**`   | публично                                              |
| `/api/orders/manage/**` | `ORDER_MANAGER`                                       |
| `/api/orders/dev/**`    | `ORDER_MANAGER` (и только с профилем `dev`)           |
| `/internal/**`          | `INTERNAL_SERVICE` (сейчас внутренних эндпоинтов нет) |
| всё остальное           | аутентифицированный пользователь                      |

Ответы 401 и 403 приходят в том же формате, что и остальные ошибки.

Токены, полученные внутри docker-сети и снаружи, должны иметь одинаковый `iss`: в Keycloak зафиксирован hostname (
`KC_HOSTNAME`), иначе вызовы в product-service и inventory-service получают 401.

## Формат ошибок

Все ошибки возвращаются в формате RFC 9457 (`application/problem+json`) через `ApiProblem`: `title`, `status`, `detail`,
`instance`, `code`, `timestamp`, `traceId`, `source`, `errors[]` (`field`, `message`). Ошибки логируются в одной точке,
`GlobalExceptionHandler`: 5xx как `ERROR` со стеком, 4xx как `WARN` одной строкой.

| Код                                                     | Статус   | Когда                                                                          |
|---------------------------------------------------------|----------|--------------------------------------------------------------------------------|
| `VALIDATION_FAILED`, `BAD_REQUEST`                      | 400      | Невалидное тело или параметры, нет `Idempotency-Key`                           |
| `UNAUTHORIZED`, `FORBIDDEN`                             | 401, 403 | Нет токена, недостаточно прав                                                  |
| `ORDER_NOT_FOUND`                                       | 404      | Заказа нет или он чужой                                                        |
| `ITEMS_UNAVAILABLE`                                     | 409      | SKU не найден или неактивен (`errors[]` по SKU)                                |
| `INSUFFICIENT_STOCK`                                    | 409      | Не хватает остатка (`errors[]` по SKU, фактическое количество не раскрывается) |
| `INVALID_ORDER_TRANSITION`                              | 409      | Переход недопустим из текущего статуса                                         |
| `RETURN_WINDOW_EXPIRED`                                 | 409      | Окно возврата истекло                                                          |
| `RESERVATION_RELEASED`, `RESERVATION_ALREADY_COMMITTED` | 409      | Конфликт состояния резерва в inventory                                         |
| `CONCURRENT_MODIFICATION`                               | 409      | Параллельное изменение заказа                                                  |
| `UPSTREAM_ERROR`                                        | 502      | Зависимость вернула неожиданный 4xx (обычно ошибка конфигурации)               |
| `SERVICE_UNAVAILABLE`                                   | 503      | Зависимость или БД недоступна, таймаут, не получен service token               |
| `INTERNAL_ERROR`                                        | 500      | Непредвиденная ошибка                                                          |

## Вызовы других сервисов

Клиенты описаны интерфейсами `@HttpExchange` (`ProductClient`, `InventoryClient`), прокси создаёт
`ServiceClientFactory`.

- **Service token**: `client_credentials`, токен кэшируется и перевыпускается Spring Security.
- **Таймауты**: подключение 2 с, чтение 5 с (`app.clients.*`).
- **Ошибки**: 5xx, таймаут и сетевые сбои дают `503`; неожиданный 4xx даёт `502`. Ошибки inventory разбираются по `code`
  из тела ответа (`UpstreamErrorMapper`).
- **Трассировка**: используется автоконфигурированный `RestClient.Builder`, поэтому `traceId` передаётся в вызываемые
  сервисы.
- **Fallback и CircuitBreaker не используются**: подставить нечего (цены и остатки нужны актуальные), а явные таймауты
  достаточно ограничивают ущерб при текущей нагрузке. Retry для вызовов inventory безопасен (идемпотентны по `orderId`),
  добавим при необходимости.

## События Kafka

Владелец топика создаёт и сам топик, и его `.DLT`.

### Публикуемые: топик `order-events`

- 3 партиции, ключ сообщения `orderId` (порядок событий одного заказа сохраняется), формат Avro, схема
  `order-event.avsc`.
- Доставка **at least once**: потребители должны быть идемпотентными.
- Событие несёт состояние заказа на момент перехода: `eventId`, `type`, `occurredAt`, `orderId`, `userId`, `status`,
  `totalAmount` (строка, UAH), `items[] { sku, quantity, unitPrice }`. Данные доставки не включены (персональные
  данные).

| Событие                  | Когда отправляется                                      |
|--------------------------|---------------------------------------------------------|
| `ORDER_CREATED`          | Заказ создан                                            |
| `ORDER_PAID`             | Оплата прошла                                           |
| `ORDER_CANCELLED`        | Заказ отменён (покупателем или из-за неуспешной оплаты) |
| `ORDER_EXPIRED`          | Заказ истёк                                             |
| `ORDER_SHIPPED`          | Заказ отправлен                                         |
| `ORDER_RETURN_REQUESTED` | Покупатель запросил возврат                             |
| `ORDER_RETURNED`         | Возврат подтверждён (inventory возвращает остаток)      |

### Потребляемые

Группа потребителей `order-service`, ошибочные сообщения после повторов (3 попытки, пауза 1 с с множителем 2, максимум
10 с) уходят в `<топик>.DLT`. Ошибки, которым повтор не поможет (заказ не найден, недопустимый переход, конфликт
резерва), уходят в DLT сразу.

| Топик                | Событие               | Действие                                                                                   |
|----------------------|-----------------------|--------------------------------------------------------------------------------------------|
| `payment-events`     | `PAYMENT_SUCCEEDED`   | `commit` резерва, `PAID`                                                                   |
| `payment-events`     | `PAYMENT_FAILED`      | `release` резерва, `CANCELLED`                                                             |
| `reservation-events` | `RESERVATION_EXPIRED` | `EXPIRED`; для неизвестного заказа или заказа не в `AWAITING_PAYMENT` событие игнорируется |

Топик `payment-events` (и его DLT) пока создаётся в order-service, после появления payment-service он переедет туда.
Неизвестный `type` пропускается с предупреждением.

### Transactional outbox

1. Смена статуса и запись в таблицу `outbox` происходят в одной транзакции.
2. `OutboxRelay` каждую секунду (`app.outbox.poll-delay-ms`) берёт `PENDING`-записи (`FOR UPDATE SKIP LOCKED`, поэтому
   безопасно при нескольких экземплярах), отправляет в Kafka по порядку и останавливается на первом сбое.
3. Успешные записи помечаются `SENT` и удаляются `OutboxCleaner` через `app.outbox.retention-days` (7) дней.

## Конфигурация

Настройки задаются переменными окружения (в compose они уже заданы).

| Переменная                                                                              | Назначение                                                          |
|-----------------------------------------------------------------------------------------|---------------------------------------------------------------------|
| `ORDER_DB_HOST`, `ORDER_DB_PORT`, `ORDER_DB_NAME`, `ORDER_DB_USER`, `ORDER_DB_PASSWORD` | PostgreSQL заказов                                                  |
| `KEYCLOAK_ISSUER_URI`                                                                   | Значение `iss` в токенах Keycloak (внешний адрес)                   |
| `KEYCLOAK_JWK_SET_URI`                                                                  | Адрес ключей Keycloak внутри сети Docker                            |
| `KEYCLOAK_TOKEN_URI`                                                                    | Адрес получения service token внутри сети Docker                    |
| `ORDER_SERVICE_SECRET`                                                                  | Секрет клиента `order-service` в Keycloak                           |
| `PRODUCT_SERVICE_URL`, `INVENTORY_SERVICE_URL`                                          | Адреса зависимостей (`http://inventory-service:8083`)               |
| `KAFKA_BOOTSTRAP_SERVERS`                                                               | Брокер Kafka (`kafka:9092`)                                         |
| `SCHEMA_REGISTRY_URL`                                                                   | Schema Registry (`http://schema-registry:8081`)                     |
| `SPRING_PROFILES_ACTIVE`                                                                | `dev` включает dev-эндпоинт оплаты. Без профиля режим прод-подобный |

Параметры приложения (`application.yaml`):

| Параметр                                                           | Назначение                                  |
|--------------------------------------------------------------------|---------------------------------------------|
| `app.order.return-window-days`                                     | Окно возврата от отправки, дней (14)        |
| `app.clients.<сервис>.base-url`, `connect-timeout`, `read-timeout` | Клиенты product-service и inventory-service |
| `app.kafka.topics.*`, `app.kafka.retry.*`                          | Названия топиков и повторы консьюмеров      |
| `app.outbox.poll-delay-ms`, `app.outbox.retention-days`            | Relay и очистка outbox                      |

Секреты хранятся в корневом файле `.env` проекта (в репозиторий не коммитится).

## Запуск

Из корня проекта:

```
docker compose up -d --build
docker compose ps
```

Обычный запуск подхватывает `docker-compose.override.yaml` (dev-доступ с хоста и профиль `dev`). Прод-подобный запуск
без публикации портов:

```
docker compose -f docker-compose.yaml up -d --build
```

| Сервис             | Адрес (dev, на хосте)                    |
|--------------------|------------------------------------------|
| order-service      | `http://127.0.0.1:8084`                  |
| PostgreSQL заказов | `127.0.0.1:5435` (порт задан в override) |

Доступ к базе:

```
docker compose exec order-db psql -U <ORDER_DB_USER> -d <ORDER_DB_NAME>
```

Сборка (`avro-maven-plugin` генерирует классы из `src/main/avro/*.avsc`):

```
mvn clean package
```

Для тестовых запросов нужны пользователи realm: `customer` (покупатель) и `orderuser` (менеджер, роль `ORDER_MANAGER`).

## Наблюдаемость

- **Health:** `/actuator/health`, а для compose `/actuator/health/readiness`.
- **Трассировка:** Micrometer Tracing кладёт `traceId` и `spanId` в MDC и передаёт `traceId` в исходящие вызовы.
  `traceId` есть в строках лога и в теле ошибки. Экспорт во внешние системы выключен.
- **Остановка:** `server.shutdown: graceful`, текущие запросы и обработка Kafka доделываются перед остановкой.
- **Kafka UI** показывает топики, сообщения (Avro расшифровывается через Schema Registry) и группы потребителей.

## Планы и ограничения

- Возврат денег при `RETURNED` и при оплате уже истёкшего заказа (нужен payment-service).
- payment-service: заменит dev-эндпоинт и будет публиковать `payment-events`.
- cart-service: решение о необходимости отложено.
- Retry для вызовов inventory (операции идемпотентны), CircuitBreaker при росте нагрузки.
- При `RETURNED` весь товар возвращается на склад; различать «годен к продаже» и «списан» пока нельзя.
- Общий DLT на несколько читателей: повторная отправка из DLT в основной топик доставила бы запись всем читателям. Для
  адресного переигрывания понадобится DLT на каждую группу.
- Данные доставки в событиях появятся, когда их запросит notification-service.
- Централизованные логи через ELK.
- Интеграционные тесты (Testcontainers: PostgreSQL, Kafka, Schema Registry).
- `auto.register.schemas` включён только для dev, в проде схемы нужно регистрировать осознанно.