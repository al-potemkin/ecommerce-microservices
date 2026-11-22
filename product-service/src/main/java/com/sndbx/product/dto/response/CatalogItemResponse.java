package com.sndbx.product.dto.response;

import com.sndbx.product.model.Gender;

import java.math.BigDecimal;
import java.util.List;

public record CatalogItemResponse(
        String id,
        String name,
        String brand,
        Gender gender,
        String mainImage,
        BigDecimal minPrice,
        List<Integer> volumes,
        boolean inStock
) {
}