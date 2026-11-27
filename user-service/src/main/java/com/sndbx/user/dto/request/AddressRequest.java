package com.sndbx.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Schema(description = "City", example = "Kyiv")
        @Size(max = 100) String city,

        @Schema(description = "Street, building and apartment", example = "Khreshchatyk St, 22, apt. 5")
        @Size(max = 255) String addressLine,

        @Schema(description = "Postal code", example = "01001")
        @Size(max = 10) String postalCode
) {
}