package com.sndbx.product.model;

import java.math.BigDecimal;
import java.util.List;

public record CatalogItemView(
        String id,
        String name,
        String brand,
        Gender gender,
        String mainImage,
        List<Variant> matchedVariants,
        BigDecimal minPrice,
        boolean inStock) {
}