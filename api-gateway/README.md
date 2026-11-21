# api-gateway

The single entry point for clients of the e-commerce system. It hides the internal services, validates JWTs issued by Keycloak, rate-limits requests, protects against cascading failures, and returns errors in a unified format.

## Features

- Routing requests to `product-service`, `order-service` and `inventory-service`.
- JWT validation (signature, expiration, issuer) and role checks before a request reaches a service.
- Allow-list of public paths: reading the product catalog and health.
- Forwarding the original token and the end-to-end `traceparent` to the services.
- Circuit breaker and timeouts.
- Rate limiting per user (by the token's `sub`) and per IP, with counters in Redis.
- CORS for the frontend.
- Unified error format based on RFC 9457 (Problem Details).
- OpenAPI documentation of all services in a single Swagger UI (`dev` profile only).

**Stack:** Java 21, Spring Boot 4, Spring Cloud Gateway, Spring Security OAuth2 Resource Server, Resilience4j, Reactive Redis, Micrometer Tracing, springdoc-openapi.

## Request flow

1. `IpRateLimitFilter` (runs before Spring Security) counts requests per IP in Redis. It sees anonymous requests, nonexistent paths and preflight requests as well.
2. Spring Security validates the token and permissions using the rules in the table below.
3. The gateway finds the route. Default filters apply to every route: `DedupeResponseHeader` and `RequestRateLimiter` (per-user limit).
4. The `CircuitBreaker` filter forwards the request to the service, applying timeouts. On failure the request is redirected to `/fallback/{service}`.
5. Every gateway error is built by a single `ErrorResponder` in the `ApiProblem` format.

## Routes and access

| Path | Method | Access |
|---|---|---|
| `/api/products/**` | GET | public, no token |
| `/api/products/**` | others | role `PRODUCT_MANAGER` |
| `/api/orders/**` | any | any valid token |
| `/api/inventory/**` | any | role `INVENTORY_MANAGER` |
| `/actuator/health/**` | GET | public |
| `/actuator/**` (other endpoints) | any | valid token, and only if the endpoint is exposed (`dev`) |
| any `OPTIONS` request | OPTIONS | public (CORS preflight, answered by the gateway itself) |
| `/swagger-ui/**`, `/v3/api-docs/**`, `/aggregate/**` | GET | public, **`dev` only** |
| everything else | any | valid token |

Rules are evaluated top to bottom and the first match wins, so public paths come first and the last rule locks down everything else. A customer can see the catalog of active products but not the stock: stock lives in `inventory-service`, which only the warehouse manager can access.

> A request without a token to a nonexistent path gets 401, not 404: security runs before routing, so an outsider cannot discover which paths exist.

## Error format

```json
{
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

| Field | Purpose |
|---|---|
| `title` | short title |
| `status` | HTTP status code |
| `detail` | safe description |
| `instance` | original request path without the query string |
| `code` | machine-readable code |
| `timestamp` | time in UTC, ISO-8601 |
| `traceId` | identifier for finding the request in the logs |
| `source` | the service that produced the error |
| `errors` | validation errors `[field, message]` |

## Rate limit headers

The per-user limiter adds `X-RateLimit-Remaining`, `X-RateLimit-Burst-Capacity` and `X-RateLimit-Replenish-Rate` to responses.

## Keycloak

The `ecommerce` realm is created by importing `keycloak/import/ecommerce-realm.json` when the container starts.

> The import only runs on an empty database. To apply changes to the file, recreate the environment: `docker compose down -v`, then `docker compose up -d` (Keycloak data will be deleted).

- **Roles:** `PRODUCT_MANAGER` (product-service), `INVENTORY_MANAGER` (inventory-service), `INTERNAL_SERVICE` (calls between services).
- **Clients:**
  - `ecommerce-web`: public frontend client, Authorization Code + PKCE (S256); sign-up and sign-in happen on the Keycloak page.
  - `ecommerce-dev`: obtains a token by username and password via curl. For development only.
  - service clients (`notification-service`, `product-service`, `order-service`, `inventory-service`, `analytics-service`, `dlt-processor-service`): client credentials, each with its own secret and the `INTERNAL_SERVICE` role.
- **Tokens:** the access token lives for 5 minutes; refresh tokens are rotated, so the old one becomes invalid after it is exchanged. A service account token is issued without a refresh token.
- **Test users:** `testuser` (both manager roles), `productuser` (`PRODUCT_MANAGER`), `inventoryuser` (`INVENTORY_MANAGER`).

## Running

```docker
docker compose up -d --build

# rebuild only the gateway
docker compose up -d --build api-gateway

# dev profile
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build api-gateway
```

Getting a token:

```bash
curl -X POST 'http://localhost:8180/realms/ecommerce/protocol/openid-connect/token' \
--data 'grant_type=password' \
--data 'client_id=ecommerce-dev' \
--data 'username=<TEST_USER_USERNAME>' \
--data 'password=<TEST_USER_PASSWORD>'
```