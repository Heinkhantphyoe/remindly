package com.remindly.notification;

import com.remindly.reminder.Reminder;
import com.remindly.reminder.ReminderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulingServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private NotificationRepository notificationRepository;

    private final Instant now = Instant.parse("2026-10-13T10:00:00Z");
    private NotificationSchedulingService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        service = new NotificationSchedulingService(reminderRepository, notificationRepository, clock, 24);
        when(notificationRepository.insertIfAbsent(any(), any(), any(), any(), any())).thenReturn(1);
        when(reminderRepository.findByCompletedFalseAndDueAtBetween(any(), any())).thenReturn(List.of());
        when(reminderRepository.findByCompletedFalseAndAdvanceOffsetMinutesIsNotNullAndDueAtAfter(any()))
                .thenReturn(List.of());
    }

    @Test
    void createsDueNotificationForOverdueReminderWithinRecoveryWindow() {
        Reminder reminder = reminder("2026-10-13T09:30:00Z", null);
        when(reminderRepository.findByCompletedFalseAndDueAtBetween(
                Instant.parse("2026-10-12T10:00:00Z"), now)).thenReturn(List.of(reminder));

        assertThat(service.processNow()).isEqualTo(1);

        verify(notificationRepository).insertIfAbsent(any(), any(), any(),
                org.mockito.ArgumentMatchers.eq("DUE"), org.mockito.ArgumentMatchers.eq(reminder.getDueAt()));
    }

    @Test
    void createsAdvanceNotificationWhenAdvanceTimeHasArrived() {
        Reminder reminder = reminder("2026-10-13T10:20:00Z", 30);
        when(reminderRepository.findByCompletedFalseAndAdvanceOffsetMinutesIsNotNullAndDueAtAfter(now))
                .thenReturn(List.of(reminder));

        assertThat(service.processNow()).isEqualTo(1);

        verify(notificationRepository).insertIfAbsent(any(), any(), any(),
                org.mockito.ArgumentMatchers.eq("ADVANCE"),
                org.mockito.ArgumentMatchers.eq(Instant.parse("2026-10-13T09:50:00Z")));
    }

    @Test
    void skipsAdvanceNotificationOutsideRecoveryWindow() {
        Reminder reminder = reminder("2026-10-14T11:00:00Z", 1800);
        when(reminderRepository.findByCompletedFalseAndAdvanceOffsetMinutesIsNotNullAndDueAtAfter(now))
                .thenReturn(List.of(reminder));

        assertThat(service.processNow()).isZero();
        verify(notificationRepository, org.mockito.Mockito.never())
                .insertIfAbsent(any(), any(), any(), any(), any());
    }

    @Test
    void usesDatabaseConflictProtectionForRepeatedRuns() {
        Reminder reminder = reminder("2026-10-13T09:30:00Z", null);
        when(reminderRepository.findByCompletedFalseAndDueAtBetween(
                Instant.parse("2026-10-12T10:00:00Z"), now)).thenReturn(List.of(reminder));
        when(notificationRepository.insertIfAbsent(any(), any(), any(), any(), any())).thenReturn(0);

        assertThat(service.processNow()).isZero();
        ArgumentCaptor<String> type = ArgumentCaptor.forClass(String.class);
        verify(notificationRepository).insertIfAbsent(any(), any(), any(), type.capture(), any());
        assertThat(type.getValue()).isEqualTo("DUE");
    }

    private Reminder reminder(String dueAt, Integer advanceMinutes) {
        return new Reminder(UUID.randomUUID(), "Reminder", null, null,
                Instant.parse(dueAt), advanceMinutes);
    }
}
