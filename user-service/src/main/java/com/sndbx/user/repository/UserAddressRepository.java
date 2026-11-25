package com.sndbx.user.repository;

import com.sndbx.user.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findAllByProfileIdOrderByDefaultAddressDescIdAsc(Long profileId);

    Optional<UserAddress> findByIdAndProfileId(Long id, Long profileId);

    Optional<UserAddress> findByProfileIdAndDefaultAddressTrue(Long profileId);

    Optional<UserAddress> findFirstByProfileIdOrderByIdAsc(Long profileId);

    boolean existsByProfileId(Long profileId);
}
