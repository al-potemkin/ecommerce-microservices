package com.sndbx.product.dto.request;

import com.sndbx.product.model.Gender;
import com.sndbx.product.model.ScentFamily;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public record CreateProductRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 100) String brand,
        @Size(max = 4000) String description,
        @NotNull Gender gender,
        @NotEmpty Set<ScentFamily> scentFamilies,
        @NotBlank @Size(max = 500) String mainImage,
        @Size(max = 20) List<@NotBlank @Size(max = 500) String> gallery,
        @NotEmpty @Size(max = 10) List<@Valid VariantRequest> variants) {
}
