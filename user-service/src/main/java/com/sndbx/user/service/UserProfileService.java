package com.sndbx.user.service;

import com.sndbx.user.dto.UpdateProfileRequest;
import com.sndbx.user.entity.UserProfile;
import com.sndbx.user.mapper.UserProfileMapper;
import com.sndbx.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;
    private final UserProfileMapper mapper;

    public UserProfile getOrCreate(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return repository.findByKeycloakUserId(userId)
                .orElseGet(() -> create(userId, jwt));
    }

    public UserProfile update(Jwt jwt, UpdateProfileRequest request) {
        UserProfile profile = getOrCreate(jwt);
        mapper.update(request, profile);
        return repository.save(profile);
    }

    private UserProfile create(UUID userId, Jwt jwt) {
        UserProfile profile = new UserProfile();
        profile.setKeycloakUserId(userId);
        profile.setFirstName(jwt.getClaimAsString("given_name"));
        profile.setLastName(jwt.getClaimAsString("family_name"));
        profile.setEmail(jwt.getClaimAsString("email"));
        try {
            return repository.save(profile);
        } catch (DataIntegrityViolationException e) {
            // two first requests at the same time: the other one created the profile
            return repository.findByKeycloakUserId(userId).orElseThrow(() -> e);
        }
    }
}