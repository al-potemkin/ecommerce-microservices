package com.sndbx.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @Schema(description = "Ordered items, 1 to 50")
        @NotEmpty @Size(max = 50) List<@Valid OrderItemRequest> items,

        @Schema(description = "Delivery data")
        @NotNull @Valid DeliveryRequest delivery
) {
}