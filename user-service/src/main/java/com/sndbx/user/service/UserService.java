package com.sndbx.user.service;

import com.sndbx.common.exception.data.ApiProblem;
import com.sndbx.user.dto.request.AddressRequest;
import com.sndbx.user.dto.request.UpdateProfileRequest;
import com.sndbx.user.entity.UserAddress;
import com.sndbx.user.entity.UserProfile;
import com.sndbx.user.exception.InvalidAddressException;
import com.sndbx.user.mapper.UserAddressMapper;
import com.sndbx.user.mapper.UserProfileMapper;
import com.sndbx.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository repository;
    private final UserProfileMapper mapper;
    private final UserAddressMapper addressMapper;

    @Transactional(readOnly = true)
    public UserProfile getProfile(Jwt jwt) {
        return findOrBuild(jwt);
    }

    @Transactional
    public UserProfile updateProfile(Jwt jwt, UpdateProfileRequest request) {
        UserProfile profile = findOrBuild(jwt);
        mapper.update(request, profile);
        if (request.address() != null) {
            applyAddress(profile, request.address(), "address.");
        }
        return repository.save(profile);
    }

    @Transactional
    public UserProfile updateAddress(Jwt jwt, AddressRequest request) {
        UserProfile profile = findOrBuild(jwt);
        applyAddress(profile, request, "");
        return repository.save(profile);
    }

    private UserProfile findOrBuild(Jwt jwt) {
        UUID id = UUID.fromString(jwt.getSubject());
        return repository.findById(id).orElseGet(() -> {
            UserProfile profile = new UserProfile();
            profile.setId(id);
            profile.setFirstName(jwt.getClaimAsString("given_name"));
            profile.setLastName(jwt.getClaimAsString("family_name"));
            profile.setEmail(jwt.getClaimAsString("email"));
            return profile;
        });
    }

    private void applyAddress(UserProfile profile, AddressRequest request, String fieldPrefix) {
        UserAddress address = profile.getAddress();
        if (address == null) {
            address = new UserAddress();
            address.setProfile(profile);
            profile.setAddress(address);
        }
        addressMapper.update(request, address);
        validate(address, fieldPrefix);
    }

    private void validate(UserAddress address, String fieldPrefix) {
        boolean hasPostalCode = address.getPostalCode() != null;
        List<ApiProblem.FieldError> errors = new ArrayList<>();
        if (address.getCity() == null && (address.getAddressLine() != null || hasPostalCode)) {
            errors.add(new ApiProblem.FieldError(fieldPrefix + "city", "City is required"));
        }
        if (address.getAddressLine() == null && (address.getCity() != null || hasPostalCode)) {
            errors.add(new ApiProblem.FieldError(fieldPrefix + "addressLine", "Address line is required"));
        }
        if (!errors.isEmpty()) {
            throw new InvalidAddressException("Address is incomplete", errors);
        }
    }
}