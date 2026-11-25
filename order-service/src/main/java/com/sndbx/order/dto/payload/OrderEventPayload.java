package com.sndbx.order.dto.payload;

import com.sndbx.order.entity.OrderStatus;
import com.sndbx.order.entity.outbox.OrderOutboxType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderEventPayload(
        String eventId,
        OrderOutboxType type,
        Instant occurredAt,
        String orderId,
        String userId,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderEventItem> items
) {
}
