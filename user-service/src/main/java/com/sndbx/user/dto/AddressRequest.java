package com.sndbx.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 200) String recipientName,
        @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Phone must be in E.164 format, e.g. +380501234567")
        String phone,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 255) String addressLine,
        @Size(max = 10) String postalCode
) {
}
