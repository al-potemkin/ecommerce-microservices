package com.sndbx.order.dto.response;

import com.sndbx.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID id,
        UUID userId,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant expiresAt,
        Instant createdAt
) {
}
