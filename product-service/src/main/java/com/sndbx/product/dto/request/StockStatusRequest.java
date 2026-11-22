package com.sndbx.product.dto.request;

import jakarta.validation.constraints.NotNull;

public record StockStatusRequest(@NotNull Boolean inStock) {
}