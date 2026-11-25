package com.sndbx.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DeliveryRequest(
        @NotBlank @Size(max = 200) String recipientName,
        @NotBlank @Pattern(regexp = "^\\+380\\d{9}$", message = "must match +380XXXXXXXXX") String recipientPhone,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 300) String address
) {
}