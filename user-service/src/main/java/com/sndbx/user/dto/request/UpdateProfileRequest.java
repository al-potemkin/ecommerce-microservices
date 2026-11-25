package com.sndbx.user.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Email @Size(max = 255) String email,
        @Pattern(regexp = "^\\+[1-9]\\d{7,11}$", message = "Phone must be in E.164 format, e.g. +380501234567")
        String phone,
        @Valid AddressRequest address
) {
}
