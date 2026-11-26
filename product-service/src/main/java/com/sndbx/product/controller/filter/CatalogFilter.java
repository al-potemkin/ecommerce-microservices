package com.sndbx.product.controller.filter;

import com.sndbx.product.dto.CatalogSort;
import com.sndbx.product.model.Gender;
import com.sndbx.product.model.ScentFamily;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Set;

public record CatalogFilter(
        @Schema(description = "Brand names, multiple allowed (OR within the filter)")
        Set<String> brand,

        @Schema(description = "Target audience, multiple allowed")
        Set<Gender> gender,

        @Schema(description = "Scent families; a product matches if it has at least one of them")
        Set<ScentFamily> scent,

        @Schema(description = "Volumes in ml, multiple allowed")
        Set<Integer> volume,

        @Schema(description = "Minimum variant price")
        @PositiveOrZero BigDecimal priceMin,

        @Schema(description = "Maximum variant price")
        @PositiveOrZero BigDecimal priceMax,

        @Schema(description = "true: only products in stock")
        Boolean inStock,

        @Schema(description = "Page number starting from 0 (default 0)", example = "0")
        @Min(0) Integer page,

        @Schema(description = "Page size, 1 to 100 (default 20)", example = "20")
        @Min(1) @Max(100) Integer size,

        @Schema(description = "Sort order (default NEWEST)")
        CatalogSort sort
) {

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