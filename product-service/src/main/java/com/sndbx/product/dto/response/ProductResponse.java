package com.sndbx.product.dto.response;

import com.sndbx.product.model.Gender;
import com.sndbx.product.model.ScentFamily;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record ProductResponse(
        String id,
        String name,
        String brand,
        String description,
        Gender gender,
        Set<ScentFamily> scentFamilies,
        String mainImage,
        List<String> gallery,
        List<VariantResponse> variants,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}