package com.sndbx.user.mapper;

import com.sndbx.user.dto.request.UpdateProfileRequest;
import com.sndbx.user.dto.response.ProfileResponse;
import com.sndbx.user.entity.UserProfile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    @Mapping(target = "address", source = "address")
    ProfileResponse toResponse(UserProfile profile);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    void update(UpdateProfileRequest request, @MappingTarget UserProfile profile);
}
