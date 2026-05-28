package com.example.meetingapp.user.service;

import com.example.meetingapp.outbox.OutboxService;
import com.example.meetingapp.user.dto.CreateUserRequest;
import com.example.meetingapp.user.dto.UpdateStatusRequest;
import com.example.meetingapp.user.dto.UpdateUserRequest;
import com.example.meetingapp.user.dto.UserResponse;
import com.example.meetingapp.user.entity.Role;
import com.example.meetingapp.user.entity.User;
import com.example.meetingapp.user.entity.UserInfo;
import com.example.meetingapp.user.entity.UserStatus;
import com.example.meetingapp.user.kafka.UserCreatedEvent;
import com.example.meetingapp.user.kafka.UserDeletedEvent;
import com.example.meetingapp.user.kafka.UserStatusChangedEvent;
import com.example.meetingapp.user.kafka.UserUpdatedEvent;
import com.example.meetingapp.user.mapper.UserMapper;
import com.example.meetingapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private OutboxService outboxService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper, outboxService);
    }

    @Test
    void createUser_setsInitialAggregateVersionAndPublishesIt() {
        CreateUserRequest request = new CreateUserRequest("alice", "a@test.local", "Alice", "Smith");
        User user = userWithVersion(0L, UserStatus.ACTIVE);
        user.setUsername(request.username());
        user.setEmail(request.email());
        UserInfo userInfo = new UserInfo();
        UserResponse response = new UserResponse(user.getId(), user.getUsername(), user.getEmail(), UserStatus.ACTIVE, Role.USER, null, null);

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(userMapper.toUserInfo(request)).thenReturn(userInfo);
        when(userRepository.saveAndFlush(user)).thenAnswer(invocation -> {
            user.setVersion(0L);
            return user;
        });
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse actual = userService.createUser(request);

        assertSame(response, actual);
        assertEquals(0L, user.getVersion());

        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(outboxService).enqueueEvent(eq("USER"), eq(user.getId().toString()), eventCaptor.capture());
        assertEquals(0L, eventCaptor.getValue().version());
        assertEquals(user.getId(), eventCaptor.getValue().userId());
    }

    @Test
    void updateUser_incrementsAggregateVersionAndPublishesIt() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest("bob-new", "new@test.local", null, null, null, null);
        User user = userWithVersion(4L, UserStatus.ACTIVE);
        user.setId(userId);
        user.setUsername("bob");
        user.setEmail("old@test.local");
        UserResponse response = new UserResponse(userId, "bob-new", "new@test.local", UserStatus.ACTIVE, Role.USER, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.saveAndFlush(user)).thenAnswer(invocation -> {
            user.setVersion(5L);
            return user;
        });
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse actual = userService.updateUser(userId, request);

        assertSame(response, actual);
        assertEquals(5L, user.getVersion());

        ArgumentCaptor<UserUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(UserUpdatedEvent.class);
        verify(outboxService).enqueueEvent(eq("USER"), eq(userId.toString()), eventCaptor.capture());
        assertEquals(5L, eventCaptor.getValue().version());
    }

    @Test
    void updateStatus_skipsVersionIncrementAndEventWhenStatusDoesNotChange() {
        UUID userId = UUID.randomUUID();
        User user = userWithVersion(7L, UserStatus.ACTIVE);
        user.setId(userId);
        UserResponse response = new UserResponse(userId, user.getUsername(), user.getEmail(), UserStatus.ACTIVE, user.getRole(), null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse actual = userService.updateStatus(userId, new UpdateStatusRequest(UserStatus.ACTIVE));

        assertSame(response, actual);
        assertEquals(7L, user.getVersion());
        verify(userRepository, never()).save(any(User.class));
        verify(outboxService, never()).enqueueEvent(eq("USER"), eq(userId.toString()), any(UserStatusChangedEvent.class));
    }

    @Test
    void updateStatus_usesFlushedAggregateVersionInPublishedEvent() {
        UUID userId = UUID.randomUUID();
        User user = userWithVersion(7L, UserStatus.ACTIVE);
        user.setId(userId);
        UserResponse response = new UserResponse(userId, user.getUsername(), user.getEmail(), UserStatus.INACTIVE, user.getRole(), null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenAnswer(invocation -> {
            user.setVersion(8L);
            return user;
        });
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse actual = userService.updateStatus(userId, new UpdateStatusRequest(UserStatus.INACTIVE));

        assertSame(response, actual);
        assertEquals(8L, user.getVersion());

        ArgumentCaptor<UserStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(UserStatusChangedEvent.class);
        verify(outboxService).enqueueEvent(eq("USER"), eq(userId.toString()), eventCaptor.capture());
        assertEquals(8L, eventCaptor.getValue().version());
    }

    @Test
    void deleteUser_incrementsAggregateVersionAndPublishesIt() {
        UUID userId = UUID.randomUUID();
        User user = userWithVersion(2L, UserStatus.ACTIVE);
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenAnswer(invocation -> {
            user.setVersion(3L);
            return user;
        });

        userService.deleteUser(userId);

        assertEquals(3L, user.getVersion());
        assertTrue(user.getDeletedAt() != null);

        ArgumentCaptor<UserDeletedEvent> eventCaptor = ArgumentCaptor.forClass(UserDeletedEvent.class);
        verify(outboxService).enqueueEvent(eq("USER"), eq(userId.toString()), eventCaptor.capture());
        assertEquals(3L, eventCaptor.getValue().version());
    }

    private User userWithVersion(long version, UserStatus status) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setEmail("user@test.local");
        user.setStatus(status);
        user.setRole(Role.USER);
        user.setVersion(version);
        return user;
    }
}
