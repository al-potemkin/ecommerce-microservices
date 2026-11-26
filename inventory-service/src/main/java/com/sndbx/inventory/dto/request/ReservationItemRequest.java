package com.sndbx.inventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReservationItemRequest(
        @Schema(description = "Variant SKU", example = "DIOR-SAUV-100")
        @NotBlank @Size(max = 64) String sku,

        @Schema(description = "Number of units to reserve, 1 to 1000", example = "2")
        @Min(value = 1, message = "must be at least 1")
        @Max(value = 1000, message = "must not exceed 1000")
        int quantity
) {
}