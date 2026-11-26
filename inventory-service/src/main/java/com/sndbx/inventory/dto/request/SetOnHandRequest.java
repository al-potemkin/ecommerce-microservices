package com.sndbx.inventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SetOnHandRequest(
        @Schema(description = "New on-hand quantity of the SKU, 0 to 10000000", example = "120")
        @Min(value = 0, message = "must not be negative")
        @Max(value = 10_000_000, message = "must not exceed 10000000")
        int onHand
) {
}