package com.sndbx.product.dto.payload;

import com.sndbx.product.model.outbox.ProductEventType;

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