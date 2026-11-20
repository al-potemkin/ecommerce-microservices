package com.sndbx.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record UpdateVariantRequest(
        @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal price,
        Boolean active) {
}
