package com.sndbx.inventory.dto.payload;

import com.sndbx.inventory.entity.outbox.StockEventType;

import java.time.Instant;

public record InventoryEventPayload(
        String eventId,
        StockEventType type,
        Instant occurredAt,
        String sku
) {
}