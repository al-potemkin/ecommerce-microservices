package com.sndbx.user.mapper;

import com.sndbx.user.dto.request.AddressRequest;
import com.sndbx.user.dto.response.AddressResponse;
import com.sndbx.user.entity.UserAddress;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserAddressMapper {

    AddressResponse toResponse(UserAddress address);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "city", source = "city", qualifiedByName = "normalize")
    @Mapping(target = "addressLine", source = "addressLine", qualifiedByName = "normalize")
    @Mapping(target = "postalCode", source = "postalCode", qualifiedByName = "normalize")
    void update(AddressRequest request, @MappingTarget UserAddress address);

    @Named("normalize")
    default String normalize(String value) {
        if (value == null) return null;
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? null : normalized;
    }
}