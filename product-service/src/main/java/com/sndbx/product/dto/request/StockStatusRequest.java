package com.sndbx.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record StockStatusRequest(
        @Schema(description = "Whether the product is currently in stock")
        @NotNull Boolean inStock
) {
}