package com.sndbx.order.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderItemRequest(
        @NotBlank @Size(max = 64) String sku,
        @Min(value = 1, message = "must be at least 1")
        @Max(value = 1000, message = "must not exceed 1000")
        int quantity
) {
}