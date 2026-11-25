package com.sndbx.order.dto.client;

import java.math.BigDecimal;

public record VariantInfo(
        String sku,
        String productId,
        String name,
        int volumeMl,
        BigDecimal price,
        boolean active,
        boolean inStock) {
}
