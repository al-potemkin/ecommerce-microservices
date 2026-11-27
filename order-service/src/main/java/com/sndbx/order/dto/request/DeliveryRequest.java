package com.sndbx.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DeliveryRequest(
        @Schema(description = "Recipient full name", example = "Ivan Petrenko")
        @NotBlank @Size(max = 200) String recipientName,

        @Schema(description = "Recipient phone in +380XXXXXXXXX format", example = "+380501234567")
        @NotBlank @Pattern(regexp = "^\\+380\\d{9}$", message = "must match +380XXXXXXXXX") String recipientPhone,

        @Schema(description = "City", example = "Kyiv")
        @NotBlank @Size(max = 100) String city,

        @Schema(description = "Street, building and apartment", example = "Khreshchatyk St, 22, apt. 5")
        @NotBlank @Size(max = 300) String address
) {
}