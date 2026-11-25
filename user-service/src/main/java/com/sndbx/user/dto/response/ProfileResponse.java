package com.sndbx.user.dto.response;

public record ProfileResponse(
        String firstName,
        String lastName,
        String email,
        String phone,
        AddressResponse address
) {
}