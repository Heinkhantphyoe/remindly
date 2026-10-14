package com.remindly.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserId(UUID userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    Optional<Notification> findByReminderIdAndNotificationType(UUID reminderId, NotificationType notificationType);

    long countByUserIdAndReadAtIsNull(UUID userId);

    @Modifying
    @Query("update Notification n set n.readAt = :readAt where n.userId = :userId and n.readAt is null")
    int markAllRead(@Param("userId") UUID userId, @Param("readAt") Instant readAt);

    @Modifying
    @Query(value = """
            INSERT INTO notifications (id, user_id, reminder_id, notification_type, scheduled_for, created_at)
            VALUES (:id, :userId, :reminderId, :notificationType, :scheduledFor, CURRENT_TIMESTAMP)
            ON CONFLICT (reminder_id, notification_type) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("userId") UUID userId,
            @Param("reminderId") UUID reminderId,
            @Param("notificationType") String notificationType,
            @Param("scheduledFor") Instant scheduledFor
    );
}
