package com.sndbx.inventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReserveRequest(
        @Schema(description = "ID of the order the reservation belongs to")
        @NotBlank @Size(max = 64) String orderId,

        @Schema(description = "Items to reserve, 1 to 100")
        @NotEmpty @Size(max = 100) List<@Valid ReservationItemRequest> items
) {
}