package com.sndbx.inventory.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReceiveStockRequest(
        @Min(value = 1, message = "must be at least 1")
        @Max(value = 1_000_000, message = "must not exceed 1000000")
        int quantity
) {
}