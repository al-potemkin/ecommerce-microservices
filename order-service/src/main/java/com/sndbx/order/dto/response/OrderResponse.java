package com.sndbx.order.dto.response;

import com.sndbx.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID userId,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant expiresAt,
        Instant createdAt,
        DeliveryResponse delivery,
        List<OrderItemResponse> items
) {
}