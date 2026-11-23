package com.sndbx.inventory.dto.response;

import java.time.Instant;

public record StockItemResponse(
        String sku,
        String productId,
        int onHand,
        int reserved,
        int available,
        Instant updatedAt
) {
}