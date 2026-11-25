package com.sndbx.user.service;

import com.sndbx.user.dto.AddressRequest;
import com.sndbx.user.entity.UserAddress;
import com.sndbx.user.exception.config.ErrorCode;
import com.sndbx.user.exception.config.UserServiceException;
import com.sndbx.user.mapper.UserAddressMapper;
import com.sndbx.user.repository.UserAddressRepository;
import com.sndbx.user.repository.UserProfileRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository repository;
    private final UserProfileRepository profileRepository;
    private final UserAddressMapper mapper;

    @Transactional(readOnly = true)
    public List<UserAddress> list(Long profileId) {
        return repository.findAllByProfileIdOrderByDefaultAddressDescIdAsc(profileId);
    }

    @Transactional
    public UserAddress create(Long profileId, AddressRequest request) {
        UserAddress address = mapper.toEntity(request);
        address.setProfile(profileRepository.getReferenceById(profileId));
        address.setDefaultAddress(!repository.existsByProfileId(profileId)); // first address becomes default
        return repository.save(address);
    }

    @Transactional
    public UserAddress update(Long profileId, Long id, AddressRequest request) {
        UserAddress address = find(profileId, id);
        mapper.update(request, address);
        return address;
    }

    @Transactional
    public UserAddress setDefault(Long profileId, Long id) {
        UserAddress address = find(profileId, id);
        if (address.isDefaultAddress()) {
            return address;
        }
        repository.findByProfileIdAndDefaultAddressTrue(profileId).ifPresent(current -> {
            current.setDefaultAddress(false);
            repository.saveAndFlush(current); // free the unique index before setting the new default
        });
        address.setDefaultAddress(true);
        return address;
    }

    @Transactional
    public void delete(Long profileId, Long id) {
        UserAddress address = find(profileId, id);
        boolean wasDefault = address.isDefaultAddress();
        repository.delete(address);
        if (wasDefault) {
            repository.flush();
            repository.findFirstByProfileIdOrderByIdAsc(profileId)
                    .ifPresent(next -> next.setDefaultAddress(true));
        }
    }

    private UserAddress find(Long profileId, Long id) {
        return repository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new UserServiceException(ErrorCode.ADDRESS_NOT_FOUND));
    }
}
