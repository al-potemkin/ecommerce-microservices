package com.sndbx.user.dto;

public record ProfileResponse(
        String firstName,
        String lastName,
        String email,
        String phone
) {
}
