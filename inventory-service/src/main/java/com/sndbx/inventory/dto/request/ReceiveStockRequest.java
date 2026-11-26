package com.sndbx.inventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReceiveStockRequest(
        @Schema(description = "Number of units received, 1 to 1000000", example = "50")
        @Min(value = 1, message = "must be at least 1")
        @Max(value = 1_000_000, message = "must not exceed 1000000")
        int quantity
) {
}