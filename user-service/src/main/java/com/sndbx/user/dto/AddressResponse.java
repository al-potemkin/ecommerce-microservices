package com.sndbx.user.dto;

public record AddressResponse(
        Long id,
        String recipientName,
        String phone,
        String city,
        String addressLine,
        String postalCode,
        boolean defaultAddress
) {
}
