package com.sndbx.user.dto.request;

import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Size(max = 100) String city,
        @Size(max = 255) String addressLine,
        @Size(max = 10) String postalCode
) {
}