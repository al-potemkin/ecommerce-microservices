package com.sndbx.user.repository;

import com.sndbx.user.entity.UserProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    @Override
    @EntityGraph(attributePaths = "address")
    Optional<UserProfile> findById(UUID id);
}