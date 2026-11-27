package com.sndbx.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Schema(description = "First name", example = "Ivan")
        @Size(max = 100) String firstName,

        @Schema(description = "Last name", example = "Petrenko")
        @Size(max = 100) String lastName,

        @Schema(description = "Email address", example = "ivan.petrenko@example.com")
        @Email @Size(max = 255) String email,

        @Schema(description = "Phone number in E.164 format", example = "+380501234567")
        @Pattern(regexp = "^\\+[1-9]\\d{7,11}$", message = "Phone must be in E.164 format, e.g. +380501234567") String phone,

        @Schema(description = "Delivery address")
        @Valid AddressRequest address
) {
}