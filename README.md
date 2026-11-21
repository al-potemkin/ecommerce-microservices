## TODO:
1. Dockerfile привести к общему виду
2. Закрыть порты, оставить только api-gateway
3. Вынести общие классы в отдельный артефакт
4. трасировка логов сквозь сервисы


PATCH с телом {}	400 EMPTY_UPDATE --- 400 Bad request
```json
{
  "title": "Empty Update",
  "status": 400,
  "detail": "At least one field (price or active) must be provided",
  "instance": "/api/products/6b0119c4d18ab86676419a74/variants/DIOR-SAUV-1002",
  "code": "EMPTY_UPDATE",
  "timestamp": "2026-11-21T04:12:37Z",
  "traceId": "b6100ce73435168f3db334abbe0bebdc",
  "source": "product-service",
  "errors": []
}
```

пустое name	400 VALIDATION_FAILED и поле в errors[] --- same
```json
{
  "title": "Validation Failed",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/products",
  "code": "VALIDATION_FAILED",
  "timestamp": "2026-11-21T04:14:20Z",
  "traceId": "4d389ce2f5583f1d3213da2d79778f90",
  "source": "product-service",
  "errors": [
    {
      "field": "name",
      "message": "must not be blank"
    }
  ]
}
```

api-gateway & product-service
```yaml
management:
  otlp:
    metrics:
      export:
        enabled: false
  tracing:
    export:
      otlp:
        enabled: false
```

ProductController - ResponseEntity