package com.example.meetingapp.user.service;

import com.example.meetingapp.config.CacheConfig;
import com.example.meetingapp.outbox.OutboxService;
import com.example.meetingapp.user.dto.CreateUserRequest;
import com.example.meetingapp.user.dto.UpdateStatusRequest;
import com.example.meetingapp.user.dto.UpdateUserRequest;
import com.example.meetingapp.user.dto.UserResponse;
import com.example.meetingapp.user.entity.Role;
import com.example.meetingapp.user.entity.User;
import com.example.meetingapp.user.entity.UserInfo;
import com.example.meetingapp.user.entity.UserStatus;
import com.example.meetingapp.user.exception.DuplicateUserException;
import com.example.meetingapp.user.exception.UserDeletedException;
import com.example.meetingapp.user.exception.UserNotFoundException;
import com.example.meetingapp.user.kafka.*;
import com.example.meetingapp.user.mapper.UserMapper;
import com.example.meetingapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final OutboxService outboxService;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException("Username already exists: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("Email already exists: " + request.email());
        }

        User user = userMapper.toEntity(request);
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(Role.USER);

        UserInfo userInfo = userMapper.toUserInfo(request);
        userInfo.setUser(user);
        user.setUserInfo(userInfo);

        user = userRepository.save(user);
        outboxService.enqueueEvent(
                "USER",
                user.getId().toString(),
                UserCreatedEvent.of(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getStatus(),
                        user.getRole().name()
                )
        );

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_CACHE_NAME, key = "#id")
    public UserResponse getUser(UUID id) {
        User user = getUserOrElseThrow(id);
        if (user.getStatus() == UserStatus.DELETED) {
            throw new UserDeletedException(id);
        }
        return userMapper.toResponse(user);
    }

    private @NonNull User getUserOrElseThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public UserResponse findByQuery(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        User user = userRepository.findByUsernameOrEmail(normalizedQuery, normalizedQuery)
                .orElseThrow(() -> new UserNotFoundException(normalizedQuery));
        if (user.getStatus() == UserStatus.DELETED) {
            throw new UserDeletedException(user.getId());
        }
        return userMapper.toResponse(user);
    }

    @Transactional
    @CachePut(value = CacheConfig.USER_CACHE_NAME, key = "#id")
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = getUserOrElseThrow(id);
        if (user.getStatus() == UserStatus.DELETED) {
            throw new UserDeletedException(id);
        }

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new DuplicateUserException("Username already exists: " + request.username());
            }
        }
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new DuplicateUserException("Email already exists: " + request.email());
            }
        }

        userMapper.updateEntity(user, request);
        applyUserInfoPatch(user, request);
        user = userRepository.save(user);
        outboxService.enqueueEvent(
                "USER",
                user.getId().toString(),
                UserUpdatedEvent.of(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getStatus(),
                        user.getRole().name()
                )
        );

        return userMapper.toResponse(user);
    }

    private void applyUserInfoPatch(User user, UpdateUserRequest request) {
        if (request.firstName() == null && request.lastName() == null
                && request.avatarUrl() == null && request.birthDate() == null) {
            return;
        }
        UserInfo info = user.getUserInfo();
        if (info == null) {
            info = new UserInfo();
            info.setUser(user);
            info.setFirstName("");
            info.setLastName("");
            user.setUserInfo(info);
        }
        if (request.firstName() != null) {
            info.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            info.setLastName(request.lastName());
        }
        if (request.avatarUrl() != null) {
            info.setAvatarUrl(request.avatarUrl());
        }
        if (request.birthDate() != null) {
            info.setBirthDate(request.birthDate());
        }
    }

    @Transactional
    @CachePut(value = CacheConfig.USER_CACHE_NAME, key = "#id")
    public UserResponse updateStatus(UUID id, UpdateStatusRequest request) {
        User user = getUserOrElseThrow(id);
        if (user.getStatus() == UserStatus.DELETED) {
            throw new UserDeletedException(id);
        }

        UserStatus previousStatus = user.getStatus();
        var newStatus = request.status();
        user.setStatus(newStatus);
        user = userRepository.save(user);

        if (previousStatus != newStatus) {
            outboxService.enqueueEvent(
                    "USER",
                    user.getId().toString(),
                    UserStatusChangedEvent.of(id, previousStatus, newStatus)
            );
        }

        return userMapper.toResponse(user);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.USER_CACHE_NAME, key = "#id")
    public void deleteUser(UUID id) {
        User user = getUserOrElseThrow(id);
        if (user.getStatus() == UserStatus.DELETED) {
            throw new UserDeletedException(id);
        }

        user.setStatus(UserStatus.DELETED);
        user.setDeletedAt(java.time.Instant.now());
        userRepository.save(user);
        outboxService.enqueueEvent(
                "USER",
                user.getId().toString(),
                UserDeletedEvent.of(id)
        );
    }
}
