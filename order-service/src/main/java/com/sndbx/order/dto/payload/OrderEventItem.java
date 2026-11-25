package com.sndbx.order.dto.payload;

import java.math.BigDecimal;

public record OrderEventItem(String sku, int quantity, BigDecimal unitPrice) {
}
