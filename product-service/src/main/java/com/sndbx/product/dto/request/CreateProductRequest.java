package com.sndbx.product.dto.request;

import com.sndbx.product.model.Gender;
import com.sndbx.product.model.ScentFamily;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public record CreateProductRequest(
        @Schema(description = "Fragrance name", example = "Sauvage")
        @NotBlank @Size(max = 200) String name,

        @Schema(description = "Brand", example = "Dior")
        @NotBlank @Size(max = 100) String brand,

        @Schema(description = "Product description")
        @Size(max = 4000) String description,

        @Schema(description = "Target audience")
        @NotNull Gender gender,

        @Schema(description = "Fragrance families (at least one required)")
        @NotEmpty Set<ScentFamily> scentFamilies,

        @Schema(description = "Main catalog image URL", example = "https://cdn.example.com/sauvage.jpg")
        @NotBlank @Size(max = 500) String mainImage,

        @Schema(description = "Product gallery images (up to 20)")
        @Size(max = 20) List<@NotBlank @Size(max = 500) String> gallery,

        @Schema(description = "Product variants by volume")
        @NotEmpty @Size(max = 10) List<@Valid VariantRequest> variants
) {
}