package com.remindly.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

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
