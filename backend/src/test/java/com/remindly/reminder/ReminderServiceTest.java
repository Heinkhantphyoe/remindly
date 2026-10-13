package com.remindly.reminder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Test
    void createsReminderForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        Instant dueAt = Instant.parse("2026-10-20T10:00:00Z");
        when(reminderRepository.save(org.mockito.ArgumentMatchers.any(Reminder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReminderResponse response = new ReminderService(reminderRepository).create(userId,
                new CreateReminderRequest("  Prepare demo ", "Slides", " work ", dueAt, 30));

        ArgumentCaptor<Reminder> captor = ArgumentCaptor.forClass(Reminder.class);
        verify(reminderRepository).save(captor.capture());
        Reminder saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getTitle()).isEqualTo("Prepare demo");
        assertThat(saved.getCategory()).isEqualTo("work");
        assertThat(response.completed()).isFalse();
    }

    @Test
    void searchesOnlyUsingAuthenticatedUserScope() {
        UUID userId = UUID.randomUUID();
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("dueAt").ascending());
        when(reminderRepository.search(userId, false, "work", null, null, pageRequest))
                .thenReturn(new PageImpl<>(List.of()));

        new ReminderService(reminderRepository).search(userId, false, " work ", null, null, pageRequest);

        verify(reminderRepository).search(userId, false, "work", null, null, pageRequest);
    }

    @Test
    void rejectsInvalidDateRange() {
        Instant dueFrom = Instant.parse("2026-10-21T00:00:00Z");
        Instant dueTo = Instant.parse("2026-10-20T00:00:00Z");

        assertThatThrownBy(() -> new ReminderService(reminderRepository)
                .search(UUID.randomUUID(), null, null, dueFrom, dueTo, PageRequest.of(0, 20)))
                .isInstanceOf(InvalidReminderRangeException.class);
    }

    @Test
    void cannotReadAnotherUsersReminder() {
        UUID userId = UUID.randomUUID();
        UUID reminderId = UUID.randomUUID();
        when(reminderRepository.findByIdAndUserId(reminderId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new ReminderService(reminderRepository).get(userId, reminderId))
                .isInstanceOf(ReminderNotFoundException.class);
    }

    @Test
    void completionSetsAndClearsCompletionTimestamp() {
        UUID userId = UUID.randomUUID();
        UUID reminderId = UUID.randomUUID();
        Reminder reminder = new Reminder(userId, "Task", null, null,
                Instant.parse("2026-10-20T10:00:00Z"), null);
        when(reminderRepository.findByIdAndUserId(reminderId, userId)).thenReturn(Optional.of(reminder));

        ReminderService service = new ReminderService(reminderRepository);
        ReminderResponse completed = service.updateCompletion(userId, reminderId, new CompletionRequest(true));
        assertThat(completed.completed()).isTrue();
        assertThat(completed.completedAt()).isNotNull();

        ReminderResponse reopened = service.updateCompletion(userId, reminderId, new CompletionRequest(false));
        assertThat(reopened.completed()).isFalse();
        assertThat(reopened.completedAt()).isNull();
    }
}
