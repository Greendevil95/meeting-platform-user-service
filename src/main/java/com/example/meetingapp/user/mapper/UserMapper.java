package com.example.meetingapp.user.mapper;

import com.example.meetingapp.user.dto.CreateUserRequest;
import com.example.meetingapp.user.dto.UpdateUserRequest;
import com.example.meetingapp.user.dto.UserInfoResponse;
import com.example.meetingapp.user.dto.UserResponse;
import com.example.meetingapp.user.entity.User;
import com.example.meetingapp.user.entity.UserInfo;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "userInfo", ignore = true)
    User toEntity(CreateUserRequest request);

    @Mapping(target = "userInfo", source = "userInfo")
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    UserInfo toUserInfo(CreateUserRequest request);

    UserInfoResponse toUserInfoResponse(UserInfo userInfo);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "userInfo", ignore = true)
    void updateEntity(@MappingTarget User user, UpdateUserRequest request);


}
