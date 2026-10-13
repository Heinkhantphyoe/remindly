package com.remindly.reminder;

import java.time.Instant;
import java.util.UUID;

public record ReminderResponse(
        UUID id,
        String title,
        String description,
        String category,
        Instant dueAt,
        Integer advanceOffsetMinutes,
        boolean completed,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static ReminderResponse from(Reminder reminder) {
        return new ReminderResponse(
                reminder.getId(), reminder.getTitle(), reminder.getDescription(), reminder.getCategory(),
                reminder.getDueAt(), reminder.getAdvanceOffsetMinutes(), reminder.isCompleted(),
                reminder.getCompletedAt(), reminder.getCreatedAt(), reminder.getUpdatedAt()
        );
    }
}
