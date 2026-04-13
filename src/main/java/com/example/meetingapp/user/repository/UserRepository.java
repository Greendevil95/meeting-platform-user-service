package com.example.meetingapp.user.repository;

import com.example.meetingapp.user.entity.User;
import com.example.meetingapp.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findByStatusAndDeletedAtLessThanEqual(UserStatus status, Instant deletedAt, Pageable pageable);
}
