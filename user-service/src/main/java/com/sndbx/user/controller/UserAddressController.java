package com.sndbx.user.controller;

import com.sndbx.user.dto.AddressRequest;
import com.sndbx.user.dto.AddressResponse;
import com.sndbx.user.dto.ProfileResponse;
import com.sndbx.user.dto.UpdateProfileRequest;
import com.sndbx.user.mapper.UserAddressMapper;
import com.sndbx.user.mapper.UserProfileMapper;
import com.sndbx.user.service.UserAddressService;
import com.sndbx.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserProfileService profileService;
    private final UserAddressService addressService;
    private final UserAddressMapper mapper;

    @GetMapping
    public List<AddressResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toResponses(addressService.list(profileId(jwt)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(@AuthenticationPrincipal Jwt jwt,
                                  @Valid @RequestBody AddressRequest request) {
        return mapper.toResponse(addressService.create(profileId(jwt), request));
    }

    @PutMapping("/{id}")
    public AddressResponse update(@AuthenticationPrincipal Jwt jwt,
                                  @PathVariable Long id,
                                  @Valid @RequestBody AddressRequest request) {
        return mapper.toResponse(addressService.update(profileId(jwt), id, request));
    }

    @PutMapping("/{id}/default")
    public AddressResponse setDefault(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return mapper.toResponse(addressService.setDefault(profileId(jwt), id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        addressService.delete(profileId(jwt), id);
    }

    private Long profileId(Jwt jwt) {
        return profileService.getOrCreate(jwt).getId();
    }
}
