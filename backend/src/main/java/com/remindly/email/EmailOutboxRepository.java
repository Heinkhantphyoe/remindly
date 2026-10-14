package com.remindly.email;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.LockModeType;

public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, UUID> {
    @Modifying
    @Query(value = """
            INSERT INTO email_outbox (id, notification_id, recipient_email, status, attempt_count, next_attempt_at, created_at, updated_at)
            VALUES (:id, :notificationId, :recipientEmail, 'PENDING', 0, :now, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (notification_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id, @Param("notificationId") UUID notificationId,
                       @Param("recipientEmail") String recipientEmail, @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<EmailOutbox> findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
            EmailOutboxStatus status, Instant now, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<EmailOutbox> findByStatusAndProcessingStartedAtBefore(EmailOutboxStatus status, Instant cutoff);
}
