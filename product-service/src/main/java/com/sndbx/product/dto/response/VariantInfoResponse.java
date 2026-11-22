package com.sndbx.product.dto.response;

import java.math.BigDecimal;

public record VariantInfoResponse(
        String sku,
        String productId,
        String name,
        int volumeMl,
        BigDecimal price,
        boolean active,
        boolean inStock) {
}