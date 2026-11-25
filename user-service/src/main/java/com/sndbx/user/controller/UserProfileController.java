package com.sndbx.user.controller;

import com.sndbx.user.dto.ProfileResponse;
import com.sndbx.user.dto.UpdateProfileRequest;
import com.sndbx.user.mapper.UserProfileMapper;
import com.sndbx.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService service;
    private final UserProfileMapper mapper;

    @GetMapping
    public ProfileResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toResponse(service.getOrCreate(jwt));
    }

    @PutMapping
    public ProfileResponse updateMe(@AuthenticationPrincipal Jwt jwt,
                                    @Valid @RequestBody UpdateProfileRequest request) {
        return mapper.toResponse(service.update(jwt, request));
    }
}