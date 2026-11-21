package com.sndbx.product.dto;

import java.math.BigDecimal;

public record PriceRange(BigDecimal min, BigDecimal max) {
}