package com.sndbx.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record UpdateVariantRequest(
        @Schema(description = "New price. If omitted, the price remains unchanged.", example = "3290.00")
        @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal price,

        @Schema(description = "Whether the variant should be visible in the storefront. If omitted, the current value remains unchanged.")
        Boolean active
) {
}
