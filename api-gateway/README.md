# api-gateway

Единая точка входа для клиентов e-commerce системы. Скрывает внутренние сервисы, проверяет JWT от Keycloak, ограничивает частоту запросов, защищает от каскадных сбоев и отдаёт ошибки в едином формате.

## Возможности

- Маршрутизация запросов в `product-service`, `order-service` и `inventory-service`.
- Проверка JWT (подпись, срок действия, издатель) и ролей до передачи запроса сервису.
- Белый список публичных путей: чтение каталога и health.
- Передача исходного токена и сквозного `traceparent` в сервисы.
- Circuit breaker и таймауты.
- Ограничение частоты запросов: на пользователя (по `sub` токена) и на IP, счётчики в Redis.
- CORS для фронтенда.
- Единый формат ошибок на базе RFC 9457 (Problem Details).
- Документация OpenAPI всех сервисов в одном Swagger UI (только в профиле `dev`).

**Стек:** Java 21, Spring Boot 4 (Spring Framework 7), Spring Cloud Gateway (WebFlux), Spring Security OAuth2 Resource Server, Resilience4j, Reactive Redis (Lettuce), Micrometer Tracing (OpenTelemetry), springdoc-openapi (dev).

## Как обрабатывается запрос

1. `IpRateLimitFilter` (приоритет выше Spring Security) считает запросы по IP в Redis. Он видит и анонимные запросы, и несуществующие пути, и preflight.
2. Spring Security проверяет токен и права по правилам из таблицы ниже.
3. Gateway находит маршрут. Для маршрутов действуют фильтры по умолчанию: `DedupeResponseHeader` и `RequestRateLimiter` (лимит на пользователя).
4. Фильтр `CircuitBreaker` пересылает запрос сервису с таймаутами. При сбое запрос уходит на `/fallback/{service}`.
5. Любая ошибка шлюза формируется через один `ErrorResponder` в формате `ApiProblem`.

## Маршруты и доступ

| Путь | Метод | Доступ |
|---|---|---|
| `/api/products/**` | GET | публично, без токена |
| `/api/products/**` | остальные | роль `PRODUCT_MANAGER` |
| `/api/orders/**` | любой | любой валидный токен |
| `/api/inventory/**` | любой | роль `INVENTORY_MANAGER` |
| `/actuator/health/**` | GET | публично |
| `/actuator/**` (прочее) | любой | валидный токен, и только если эндпоинт опубликован (в `dev`) |
| любой метод `OPTIONS` | OPTIONS | публично (preflight CORS, ответ даёт сам шлюз) |
| `/swagger-ui/**`, `/v3/api-docs/**`, `/aggregate/**` | GET | публично, **только в `dev`** |
| всё остальное | любой | валидный токен |

Правила проверяются сверху вниз, побеждает первое совпавшее, поэтому публичные пути идут первыми, а последнее правило закрывает всё остальное. Покупатель видит каталог активных товаров, но не остатки: остатки живут в `inventory-service`, и доступ к нему только у менеджера склада.

Запрос без токена к несуществующему пути получает 401, а не 404: безопасность срабатывает раньше маршрутизации, посторонний не узнаёт, какие пути существуют.

## Формат ошибок
```json
{
  "type": "https://api.example.com/problems/unauthorized",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Authentication is required to access this resource",
  "instance": "/api/orders",
  "code": "UNAUTHORIZED",
  "timestamp": "2026-11-19T01:30:00Z",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
  "source": "api-gateway",
  "errors": []
}
```

| Поле | Назначение |
|---|---|
| `type` | идентификатор типа ошибки |
| `title` | краткое название |
| `status` | HTTP-код |
| `detail` | безопасное описание |
| `instance` | исходный путь запроса без query-строки |
| `code` | машинный код |
| `timestamp` | время в UTC, ISO-8601 |
| `traceId` | идентификатор для поиска запроса в логах |
| `source` | сервис, сформировавший ошибку |
| `errors` | ошибки валидации `[field, message]` |

Заголовки лимитера на пользователя: `X-RateLimit-Remaining`, `X-RateLimit-Burst-Capacity`, `X-RateLimit-Replenish-Rate`. Значение `Remaining: -1` означает, что Redis недоступен и запрос пропущен без подсчёта.

## Keycloak

Realm `ecommerce` создаётся импортом из `keycloak/import/ecommerce-realm.json` при старте контейнера (`--import-realm`).

- **Важно:** импорт срабатывает только на пустой базе. Чтобы применить изменения файла, пересоздайте окружение: `docker compose down -v`, затем `docker compose up -d` (данные Keycloak будут удалены).
- **Роли:** `PRODUCT_MANAGER` (product-service), `INVENTORY_MANAGER` (inventory-service), `INTERNAL_SERVICE` (вызовы между сервисами без пользователя).
- **Клиенты:**
    - `ecommerce-web`: публичный клиент фронтенда, Authorization Code + PKCE (S256), регистрация и вход через страницу Keycloak.
    - `ecommerce-dev`: получение токена по логину и паролю через curl. Только для разработки, перед реальным использованием удалить.
    - клиенты сервисов (`notification-service`, `product-service`, `order-service`, `inventory-service`, `analytics-service`, `dlt-processor-service`): client credentials, у каждого свой секрет и роль `INTERNAL_SERVICE`.
- **Токены:** access token живёт 5 минут; refresh token с ротацией (старый после обмена недействителен), поэтому украденный refresh token можно использовать один раз. Токен сервисного аккаунта выдаётся без refresh token, сервис просто запрашивает новый.
- **Тестовые пользователи:** `testuser` (обе менеджерские роли), `productuser` (`PRODUCT_MANAGER`), `inventoryuser` (`INVENTORY_MANAGER`). Пароль берётся из `TEST_USER_PASSWORD`.

## Запуск
```bash
docker compose up -d --build

docker compose up -d --build api-gateway #пересборка

docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build api-gateway #dev
```

Получение токена (живёт 5 минут):
```http request
curl -X POST 'http://localhost:8180/realms/ecommerce/protocol/openid-connect/token' \
--data 'grant_type=password' \
--data 'client_id=ecommerce-dev' \
--data 'username=<TEST_USER_USERNAME>' \
--data 'password=<TEST_USER_PASSWORD>'
```
