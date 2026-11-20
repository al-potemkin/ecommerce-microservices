package com.sndbx.product.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record VariantRequest(
        @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9._-]+") String sku,
        @Positive int volumeMl,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal price,
        Boolean active) {
}
