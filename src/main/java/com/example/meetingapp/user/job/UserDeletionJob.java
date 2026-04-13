package com.example.meetingapp.user.job;

import com.example.meetingapp.user.entity.User;
import com.example.meetingapp.user.entity.UserStatus;
import com.example.meetingapp.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDeletionJob {

    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Value("${app.user.deletion-retention-days:30}")
    private int retentionDays;

    @Value("${app.user.deletion-batch-size:500}")
    private int deletionBatchSize;

    @Scheduled(cron = "${app.user.deletion-job-cron:0 0 2 * * ?}")
    @Transactional
    public void deleteMarkedUsers() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        long totalDeleted = 0;

        while (true) {
            List<User> usersToDelete = userRepository.findByStatusAndDeletedAtLessThanEqual(
                            UserStatus.DELETED,
                            cutoff,
                            PageRequest.of(0, deletionBatchSize))
                    .getContent();

            if (usersToDelete.isEmpty()) {
                break;
            }

            List<UUID> userIds = usersToDelete.stream()
                    .map(User::getId)
                    .toList();

            userRepository.deleteAllByIdInBatch(userIds);
            entityManager.clear();

            totalDeleted += userIds.size();
            log.info("Physically deleted {} users in current batch", userIds.size());
        }

        if (totalDeleted == 0) {
            log.debug("No users to physically delete");
        } else {
            log.info("User deletion job finished. Total physically deleted users={}", totalDeleted);
        }
    }
}
