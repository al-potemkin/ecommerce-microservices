package com.sndbx.user.mapper;

import com.sndbx.user.dto.AddressRequest;
import com.sndbx.user.dto.AddressResponse;
import com.sndbx.user.entity.UserAddress;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserAddressMapper {

    AddressResponse toResponse(UserAddress address);

    List<AddressResponse> toResponses(List<UserAddress> addresses);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "recipientName", source = "recipientName")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "addressLine", source = "addressLine")
    @Mapping(target = "postalCode", source = "postalCode")
    UserAddress toEntity(AddressRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "recipientName", source = "recipientName")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "addressLine", source = "addressLine")
    @Mapping(target = "postalCode", source = "postalCode")
    void update(AddressRequest request, @MappingTarget UserAddress address);
}
