package com.sndbx.user.dto.response;

public record AddressResponse(
        String city,
        String addressLine,
        String postalCode
) {
}