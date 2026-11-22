package com.sndbx.product.dto.response;

import java.math.BigDecimal;

public record VariantResponse(String sku, int volumeMl, BigDecimal price, boolean active, boolean inStock) {
}