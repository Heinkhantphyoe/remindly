package com.remindly.reminder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;

    public ReminderService(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    @Transactional
    public ReminderResponse create(UUID userId, CreateReminderRequest request) {
        Reminder reminder = new Reminder(userId, request.title().trim(), request.description(),
                normalize(request.category()), request.dueAt(), request.advanceOffsetMinutes());
        return ReminderResponse.from(reminderRepository.save(reminder));
    }

    @Transactional(readOnly = true)
    public Page<ReminderResponse> search(UUID userId, Boolean completed, String category,
                                         Instant dueFrom, Instant dueTo, Pageable pageable) {
        if (dueFrom != null && dueTo != null && dueFrom.isAfter(dueTo)) {
            throw new InvalidReminderRangeException();
        }
        return reminderRepository.search(userId, completed, normalize(category), dueFrom, dueTo, pageable)
                .map(ReminderResponse::from);
    }

    @Transactional(readOnly = true)
    public ReminderResponse get(UUID userId, UUID reminderId) {
        return ReminderResponse.from(findOwned(userId, reminderId));
    }

    @Transactional
    public ReminderResponse update(UUID userId, UUID reminderId, UpdateReminderRequest request) {
        Reminder reminder = findOwned(userId, reminderId);
        reminder.update(request.title().trim(), request.description(), normalize(request.category()),
                request.dueAt(), request.advanceOffsetMinutes());
        return ReminderResponse.from(reminder);
    }

    @Transactional
    public ReminderResponse updateCompletion(UUID userId, UUID reminderId, CompletionRequest request) {
        Reminder reminder = findOwned(userId, reminderId);
        reminder.setCompleted(request.completed());
        return ReminderResponse.from(reminder);
    }

    @Transactional
    public void delete(UUID userId, UUID reminderId) {
        reminderRepository.delete(findOwned(userId, reminderId));
    }

    private Reminder findOwned(UUID userId, UUID reminderId) {
        return reminderRepository.findByIdAndUserId(reminderId, userId)
                .orElseThrow(ReminderNotFoundException::new);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
