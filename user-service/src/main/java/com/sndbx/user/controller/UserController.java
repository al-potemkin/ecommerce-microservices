package com.sndbx.user.controller;

import com.sndbx.user.dto.request.AddressRequest;
import com.sndbx.user.dto.request.UpdateProfileRequest;
import com.sndbx.user.dto.response.AddressResponse;
import com.sndbx.user.dto.response.ProfileResponse;
import com.sndbx.user.mapper.UserProfileMapper;
import com.sndbx.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final UserProfileMapper mapper;

    @GetMapping
    public ProfileResponse getUserProfile(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toResponse(service.getProfile(jwt));
    }

    @PutMapping
    public ProfileResponse updateUserProfile(@AuthenticationPrincipal Jwt jwt,
                                             @Valid @RequestBody UpdateProfileRequest request) {
        return mapper.toResponse(service.updateProfile(jwt, request));
    }

    @GetMapping("/address")
    public AddressResponse getAddress(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toResponse(service.getProfile(jwt)).address();
    }

    @PutMapping("/address")
    public AddressResponse updateAddress(@AuthenticationPrincipal Jwt jwt,
                                         @Valid @RequestBody AddressRequest request) {
        return mapper.toResponse(service.updateAddress(jwt, request)).address();
    }
}