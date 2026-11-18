package com.sndbx.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        List<Product> products,
        LocalDateTime orderDate,
        String comment
) {
    public record Product(
            String productId,
            Integer quantity,
            BigDecimal perItemPrice
    ) {
    }
}