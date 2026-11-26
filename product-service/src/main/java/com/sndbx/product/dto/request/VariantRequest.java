package com.sndbx.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record VariantRequest(
        @Schema(description = "Variant SKU. Cannot be changed after creation.", example = "DIOR-SAUV-100")
        @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9._-]+") String sku,

        @Schema(description = "Volume in milliliters (ml)", example = "100")
        @Positive int volumeMl,

        @Schema(description = "Price", example = "3450.00")
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal price,

        @Schema(description = "Whether the variant should be visible in the storefront.")
        Boolean active
) {
}
