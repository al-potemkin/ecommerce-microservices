package com.sndbx.inventory.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SetOnHandRequest(
        @Min(value = 0, message = "must not be negative")
        @Max(value = 10_000_000, message = "must not exceed 10000000")
        int onHand
) {
}