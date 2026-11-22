package com.sndbx.product.model.outbox;

import java.time.Instant;
import java.util.List;

public record ProductEventPayload(
        String eventId,
        ProductEventType type,
        Instant occurredAt,
        String productId,
        String name,
        boolean productActive,
        List<VariantSnapshot> variants) {

    public record VariantSnapshot(String sku, int volumeMl, boolean active) {
    }
}