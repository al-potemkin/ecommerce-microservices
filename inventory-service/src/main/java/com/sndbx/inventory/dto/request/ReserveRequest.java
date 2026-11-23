package com.sndbx.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReserveRequest(
        @NotBlank @Size(max = 64) String orderId,
        @NotEmpty @Size(max = 100) List<@Valid ReservationItemRequest> items
) {
}