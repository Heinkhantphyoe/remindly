package com.remindly.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID reminderId,
        NotificationType notificationType,
        Instant scheduledFor,
        Instant readAt,
        Instant createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(), notification.getReminderId(), notification.getNotificationType(),
                notification.getScheduledFor(), notification.getReadAt(), notification.getCreatedAt()
        );
    }
}
