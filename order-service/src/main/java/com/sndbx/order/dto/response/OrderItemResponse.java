package com.sndbx.order.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        String sku,
        String productId,
        String productName,
        int volumeMl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}