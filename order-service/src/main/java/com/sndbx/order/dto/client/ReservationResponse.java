package com.sndbx.order.dto.client;

import java.time.Instant;
import java.util.List;

public record ReservationResponse(
        String orderId,
        ReservationStatus status,
        Instant expiresAt,
        List<ReservationItem> items) {
}