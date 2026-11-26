# inventory-service

Stock and reservation service of the ecommerce microservices project. It owns stock levels per SKU, reserves stock for
orders, and tells the rest of the system when a SKU becomes available or runs out.

## Responsibilities

- Keep `on_hand` and `reserved` quantities per SKU in PostgreSQL.
- Create a stock row (with zero quantity) for every variant announced by product-service.
- Let warehouse managers receive goods and set counted quantities.
- Reserve stock for an order, then commit it (payment succeeded) or release it (payment failed, order cancelled, or the
  reservation expired).
- Publish `SKU_IN_STOCK` / `SKU_OUT_OF_STOCK` events when available quantity crosses zero.

Stock is never exposed to customers. Customers only see an `inStock` flag, which product-service maintains from this
service's events.

## Tech stack

Java 21, Spring Boot 4.1, Spring Data JPA, PostgreSQL 16, Flyway, Spring Security (OAuth2 resource server), Spring Kafka
with Avro and Schema Registry, MapStruct, Lombok.

## Concepts

- **Available quantity** is `on_hand - reserved`. It is never stored; it is computed when needed.
- **SKU** is the business key. Rows also have a surrogate `id`, which is used for foreign keys and never exposed in the
  API.
- **Reservation** is one row per SKU and order. All rows of an order share one status and one expiry time.

### Reservation lifecycle

| State       | How it is reached                                           |
|-------------|-------------------------------------------------------------|
| `RESERVED`  | `POST /internal/reservations`                               |
| `COMMITTED` | `commit` while `RESERVED` (stock is deducted)               |
| `RELEASED`  | `release` while `RESERVED`, or the expiry job after the TTL |

Typical flow driven by the order service:

1. Order is created: `reserve` all items at once. If any item is short, nothing is reserved.
2. Payment succeeds: `commit`. Payment fails or the order is cancelled: `release`.
3. If the order is never paid, the reservation expires after the TTL and is released by a background job.

Rules worth knowing:

- All calls are idempotent by `orderId`. Repeating a call returns the current state and changes nothing.
- A repeated `reserve` returns the original reservation and ignores the request body, so treat an `orderId` as
  immutable.
- Several lines with the same SKU in one request are merged and their quantities summed.
- `commit` after the reservation was released or expired returns `409 RESERVATION_RELEASED`. The caller must handle "
  paid after expiry".
- `release` after `commit` returns `409 RESERVATION_ALREADY_COMMITTED`.
- `commit` does not change available quantity, so it publishes no event.

## API

Security:

| Path                  | Role                                           |
|-----------------------|------------------------------------------------|
| `/api/inventory/**`   | `INVENTORY_MANAGER`                            |
| `/internal/**`        | `INTERNAL_SERVICE` (not routed by the gateway) |
| `/actuator/health/**` | public                                         |

Tokens are validated by the service itself. Roles are read from `realm_access.roles`.

### Manager API

| Method | Path                                             | Description                                                             |
|--------|--------------------------------------------------|-------------------------------------------------------------------------|
| GET    | `/api/inventory/stock?productId=&page=0&size=20` | Paged list sorted by SKU, `productId` optional, `size` 1 to 100         |
| GET    | `/api/inventory/stock/{sku}`                     | One stock item                                                          |
| POST   | `/api/inventory/stock/{sku}/receipts`            | Receive goods: `{"quantity": 10}` adds to `on_hand` (1 to 1,000,000)    |
| PUT    | `/api/inventory/stock/{sku}/on-hand`             | Set the counted quantity: `{"onHand": 8}`, must not be below `reserved` |

Stock items are created from `product-events`, so an unknown SKU returns `404`.

Response example:

```json
{
  "sku": "DIOR-SAV-100",
  "productId": "665f1c2e9a1b",
  "onHand": 8,
  "reserved": 3,
  "available": 5,
  "updatedAt": "2026-11-23T09:41:12.482Z"
}
```

List responses are wrapped as `content`, `page`, `size`, `totalElements`, `totalPages`.

Example through the gateway:

```
curl --location 'http://localhost:8080/api/inventory/stock/DIOR-SAV-100/receipts' \
--header 'Authorization: Bearer <TOKEN>' \
--header 'Content-Type: application/json' \
--data '{"quantity": 10}'
```

### Reservation API

| Method | Path                                       | Description                                   |
|--------|--------------------------------------------|-----------------------------------------------|
| POST   | `/internal/reservations`                   | Reserve all items of an order, all or nothing |
| POST   | `/internal/reservations/{orderId}/commit`  | Deduct reserved stock                         |
| POST   | `/internal/reservations/{orderId}/release` | Free the reservation                          |
| GET    | `/internal/reservations/{orderId}`         | Current state                                 |

Reserve request (1 to 100 items, quantity 1 to 1000 per item):

```json
{
  "orderId": "order-1",
  "items": [
    { "sku": "DIOR-SAV-100", "quantity": 3 },
    { "sku": "DIOR-SAV-50", "quantity": 1 }
  ]
}
```

Response:

```json
{
  "orderId": "order-1",
  "status": "RESERVED",
  "expiresAt": "2026-11-23T10:00:00Z",
  "items": [
    { "sku": "DIOR-SAV-100", "quantity": 3 },
    { "sku": "DIOR-SAV-50", "quantity": 1 }
  ]
}
```

If stock is short, the response is `409 INSUFFICIENT_STOCK` and `errors[]` names every short SKU, for example
`requested 3, available 2`.

Callers use a token obtained with the client credentials grant, and their service account must have the
`INTERNAL_SERVICE` realm role.

### Errors

All errors use the project-wide `ApiProblem` format (`title`, `status`, `detail`, `instance`, `code`, `timestamp`,
`traceId`, `source`, `errors[]`).

| Code                            | Status    | When                                                 |
|---------------------------------|-----------|------------------------------------------------------|
| `VALIDATION_FAILED`             | 400       | Invalid request body, with `errors[]` per field      |
| `UNAUTHORIZED` / `FORBIDDEN`    | 401 / 403 | Missing token or missing role                        |
| `STOCK_ITEM_NOT_FOUND`          | 404       | Unknown SKU                                          |
| `RESERVATION_NOT_FOUND`         | 404       | Unknown order on commit, release or get              |
| `INSUFFICIENT_STOCK`            | 409       | Not enough available quantity on reserve             |
| `STOCK_BELOW_RESERVED`          | 409       | Counted quantity is lower than the reserved quantity |
| `RESERVATION_RELEASED`          | 409       | Commit after release or expiry                       |
| `RESERVATION_ALREADY_COMMITTED` | 409       | Release after commit                                 |
| `CONCURRENT_MODIFICATION`       | 409       | Optimistic lock conflict; safe to retry              |
| `SERVICE_UNAVAILABLE`           | 503       | Database unavailable                                 |

## Concurrency

- Reserve, commit and release lock the affected stock rows (`SELECT ... FOR UPDATE`) in SKU order, so two orders that
  share SKUs cannot deadlock and the last unit cannot be oversold.
- Competing commit and release for one order are serialized by that lock: one wins, the other gets a regular `409`.
- Stock rows and reservations also carry a `version` column as a safety net, and manager updates use it to detect two
  managers editing the same SKU.

## Events

### Consumed: `product-events`

Product-service publishes a snapshot of the product, including all its variants. For every event whose type is known,
the service makes sure a stock row exists for each SKU in the snapshot:

- The insert is `ON CONFLICT DO NOTHING`, so duplicate and out-of-order events are harmless.
- Deactivation is ignored on purpose: inactive products can still receive stock.
- Events of type `UNKNOWN` are skipped with a warning.
- On first start the consumer group reads the topic from the beginning, which backfills stock rows for the existing
  catalog.

### Produced: `inventory-events`

| Type               | When                                                                                       |
|--------------------|--------------------------------------------------------------------------------------------|
| `SKU_IN_STOCK`     | Available quantity goes from zero to above zero (receipt, count, or a reservation release) |
| `SKU_OUT_OF_STOCK` | Available quantity goes from above zero to zero (count or a reservation)                   |

Details:

- The message key is the SKU, so events for one SKU keep their order inside a partition.
- The topic (3 partitions) and the Avro schema are owned by this service.
- Events are only published on a state change. A second receipt for a SKU that is already in stock publishes nothing.

### Transactional outbox

Events are written to the `outbox` table in the same transaction as the stock change, so an event is never lost and
never published without its change. A relay runs every second, locks a batch of pending rows (`FOR UPDATE SKIP LOCKED`),
sends them in order, and marks them `SENT`. If sending fails, it stops at the first failure, counts the attempt, and
retries on the next run. Delivery is at least once; consumers must be idempotent (product-service is).

Order of events per SKU is guaranteed for a single running instance.

### Dead letter queue

If processing a `product-events` message fails, the consumer retries with exponential backoff (3 retries: about 1, 2 and
4 seconds), then publishes the message to `product-events.DLT` and moves on. Messages that cannot be deserialized and
constraint violations go to the DLT immediately. DLT messages carry the original topic, partition, offset and the
exception in their headers. They are only monitored (for example in Kafka UI); there is no automatic reprocessing.

## Background jobs

| Job                 | Schedule       | What it does                                                                                                                                 |
|---------------------|----------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| Outbox relay        | every 1 s      | Publishes pending events                                                                                                                     |
| Outbox cleaner      | hourly         | Deletes `SENT` outbox rows older than 7 days                                                                                                 |
| Reservation expiry  | every 60 s     | Releases `RESERVED` reservations past `expires_at`, using the same logic as `release`                                                        |
| Reservation cleaner | daily at 03:30 | Deletes `COMMITTED` and `RELEASED` reservations older than 30 days (measured from the last status change). `RESERVED` rows are never deleted |

Deleting old reservations also forgets them for idempotency purposes, so the retention period must stay longer than any
retry window of the order service.

## Database

Schema is managed by Flyway; Hibernate only validates it (`ddl-auto: validate`).

| Migration                    | Contents                                                                                                                                                                       |
|------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `V1__create_stock_item.sql`  | `stock_item`: surrogate `id`, unique `sku`, `product_id`, `on_hand`, `reserved`, `version`, timestamps. Check constraints: both quantities non-negative, `reserved <= on_hand` |
| `V2__create_outbox.sql`      | `outbox` with a partial index on pending rows                                                                                                                                  |
| `V3__create_reservation.sql` | `reservation`: foreign key to `stock_item`, unique `(order_id, stock_item_id)`, quantity above zero, partial index for expiry lookups                                          |

The database constraints back up the application checks: even a bug cannot make stock negative or reserve more than is
on hand.

## Configuration

### Environment variables

| Variable                  | Default         | Description                                          |
|---------------------------|-----------------|------------------------------------------------------|
| `SERVER_PORT`             | `8082`          | HTTP port (docker-compose sets `8083`)               |
| `INVENTORY_DB_HOST`       | `localhost`     | PostgreSQL host                                      |
| `INVENTORY_DB_PORT`       | `5433`          | PostgreSQL port (inside compose: `5432`)             |
| `INVENTORY_DB_NAME`       | `inventory`     | Database name                                        |
| `INVENTORY_DB_USER`       | `inventory_app` | Database user                                        |
| `INVENTORY_DB_PASSWORD`   | none, required  | Database password                                    |
| `JWT_ISSUER_URI`          | none, required  | Expected token issuer                                |
| `JWT_JWK_SET_URI`         | none, required  | Keycloak JWKS endpoint, reachable from the container |
| `KAFKA_BOOTSTRAP_SERVERS` | none, required  | Kafka bootstrap servers                              |
| `SCHEMA_REGISTRY_URL`     | none, required  | Confluent Schema Registry URL                        |

### Application properties

| Property                               | Default          | Description                              |
|----------------------------------------|------------------|------------------------------------------|
| `app.kafka.topics.product-events`      | `product-events` | Topic the consumer reads                 |
| `app.kafka.retry.max-retries`          | `3`              | Retries before a message goes to the DLT |
| `app.kafka.retry.initial-interval-ms`  | `1000`           | First backoff interval                   |
| `app.kafka.retry.multiplier`           | `2.0`            | Backoff multiplier                       |
| `app.kafka.retry.max-interval-ms`      | `10000`          | Backoff cap                              |
| `app.outbox.poll-delay-ms`             | `1000`           | Relay delay between runs                 |
| `app.outbox.retention-days`            | `7`              | How long sent outbox rows are kept       |
| `app.reservation.ttl-minutes`          | `15`             | Reservation lifetime                     |
| `app.reservation.expiry-poll-delay-ms` | `60000`          | Delay between expiry job runs            |
| `app.reservation.retention-days`       | `30`             | How long finished reservations are kept  |
| `app.reservation.cleanup-cron`         | `0 30 3 * * *`   | Schedule of the reservation cleaner      |

Any property can be set through an environment variable using Spring's relaxed binding, for example
`APP_RESERVATION_TTL_MINUTES=5`.

## Running

The service is part of the project's `docker-compose.yaml` and needs `inventory-db`, `kafka`, `schema-registry` and
`keycloak` running.

```
docker compose up -d --build inventory-service
docker compose logs -f inventory-service
```

Health:

```
GET /actuator/health/readiness
```

Only the health endpoint is exposed. The service port is not published by default; to call `/internal/**` by hand,
publish it on localhost in a `docker-compose.override.yaml`:

```yaml
services:
  inventory-service:
    ports:
      - "127.0.0.1:8083:8083"
```

Inspect the database from inside its container:

```
docker compose exec inventory-db psql -U <INVENTORY_DB_USER> -d <INVENTORY_DB_NAME> -c "SELECT sku, on_hand, reserved FROM stock_item ORDER BY sku;"
```

## Not implemented yet

- An event when a reservation expires, so the order service can cancel the order without polling.
- Automatic reprocessing of messages from `product-events.DLT`.
- A stock movement journal (audit trail of every quantity change).
- Automated tests.