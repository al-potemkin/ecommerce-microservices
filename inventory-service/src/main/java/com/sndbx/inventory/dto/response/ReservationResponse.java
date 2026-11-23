package com.sndbx.inventory.dto.response;


import com.sndbx.inventory.entity.ReservationStatus;

import java.time.Instant;
import java.util.List;

public record ReservationResponse(
        String orderId,
        ReservationStatus status,
        Instant expiresAt,
        List<ReservationItemResponse> items
) {
}