package com.sndbx.user.controller.openapi;

import com.sndbx.user.dto.request.AddressRequest;
import com.sndbx.user.dto.request.UpdateProfileRequest;
import com.sndbx.user.dto.response.AddressResponse;
import com.sndbx.user.dto.response.ProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User profile", description = "Profile and delivery address of the authenticated user. Available to any authenticated user.")
public interface UserApi {

    @Operation(
            summary = "Get my profile",
            description = "Returns the profile of the authenticated user. The profile is created on first access from the token claims."
    )
    @ApiResponse(responseCode = "200", description = "Profile successfully retrieved")
    @GetMapping
    ProfileResponse getUserProfile(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt);


    @Operation(
            summary = "Update my profile",
            description = "Updates the profile of the authenticated user. All fields are optional. Phone must be in E.164 format."
    )
    @ApiResponse(responseCode = "200", description = "Profile successfully updated")
    @PutMapping
    ProfileResponse updateUserProfile(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                      @Valid @RequestBody UpdateProfileRequest request);


    @Operation(
            summary = "Get my address",
            description = "Returns the delivery address of the authenticated user. Each user has a single address."
    )
    @ApiResponse(responseCode = "200", description = "Address successfully retrieved")
    @GetMapping("/address")
    AddressResponse getAddress(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt);


    @Operation(
            summary = "Update my address",
            description = "Sets the delivery address of the authenticated user. To clear the address, send empty fields."
    )
    @ApiResponse(responseCode = "200", description = "Address successfully updated")
    @PutMapping("/address")
    AddressResponse updateAddress(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                  @Valid @RequestBody AddressRequest request);
}