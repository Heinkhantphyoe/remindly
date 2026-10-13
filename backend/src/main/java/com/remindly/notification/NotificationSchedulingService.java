package com.remindly.notification;

import com.remindly.reminder.Reminder;
import com.remindly.reminder.ReminderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationSchedulingService {

    private final ReminderRepository reminderRepository;
    private final NotificationRepository notificationRepository;
    private final Clock clock;
    private final Duration recoveryWindow;

    public NotificationSchedulingService(
            ReminderRepository reminderRepository,
            NotificationRepository notificationRepository,
            Clock clock,
            @Value("${notifications.recovery-window-hours:24}") long recoveryWindowHours
    ) {
        this.reminderRepository = reminderRepository;
        this.notificationRepository = notificationRepository;
        this.clock = clock;
        this.recoveryWindow = Duration.ofHours(recoveryWindowHours);
    }

    @Transactional
    public int processNow() {
        Instant now = clock.instant();
        Instant recoveryStart = now.minus(recoveryWindow);
        int created = 0;

        for (Reminder reminder : reminderRepository.findByCompletedFalseAndDueAtBetween(recoveryStart, now)) {
            created += createNotification(reminder, NotificationType.DUE, reminder.getDueAt());
        }

        for (Reminder reminder : reminderRepository
                .findByCompletedFalseAndAdvanceOffsetMinutesIsNotNullAndDueAtAfter(now)) {
            Instant advanceAt = reminder.getDueAt()
                    .minusSeconds(reminder.getAdvanceOffsetMinutes().longValue() * 60);
            if (!advanceAt.isBefore(recoveryStart) && !advanceAt.isAfter(now)) {
                created += createNotification(reminder, NotificationType.ADVANCE, advanceAt);
            }
        }

        return created;
    }

    private int createNotification(Reminder reminder, NotificationType type, Instant scheduledFor) {
        return notificationRepository.insertIfAbsent(
                UUID.randomUUID(), reminder.getUserId(), reminder.getId(), type.name(), scheduledFor
        );
    }
}
