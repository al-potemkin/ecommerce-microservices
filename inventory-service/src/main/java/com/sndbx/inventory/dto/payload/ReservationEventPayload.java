package com.sndbx.inventory.dto.payload;

import java.time.Instant;

public record ReservationEventPayload(String eventId, Instant occurredAt, String orderId) {
}
