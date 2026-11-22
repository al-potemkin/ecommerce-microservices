package com.sndbx.product.controller.filter;

import com.sndbx.product.dto.CatalogSort;
import com.sndbx.product.model.Gender;
import com.sndbx.product.model.ScentFamily;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Set;

public record CatalogFilter(
        Set<String> brand,
        Set<Gender> gender,
        Set<ScentFamily> scent,
        Set<Integer> volume,
        @PositiveOrZero BigDecimal priceMin,
        @PositiveOrZero BigDecimal priceMax,
        Boolean inStock,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        CatalogSort sort) {

    public CatalogFilter {
        brand = brand == null ? Set.of() : Set.copyOf(brand);
        gender = gender == null ? Set.of() : Set.copyOf(gender);
        scent = scent == null ? Set.of() : Set.copyOf(scent);
        volume = volume == null ? Set.of() : Set.copyOf(volume);
        page = page == null ? 0 : page;
        size = size == null ? 20 : size;
        sort = sort == null ? CatalogSort.NEWEST : sort;
    }

    public boolean onlyInStock() {
        return Boolean.TRUE.equals(inStock);
    }
}