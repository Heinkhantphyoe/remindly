package com.remindly.email;

import com.remindly.notification.Notification;
import com.remindly.notification.NotificationRepository;
import com.remindly.notification.NotificationType;
import com.remindly.reminder.Reminder;
import com.remindly.reminder.ReminderRepository;
import com.remindly.user.User;
import com.remindly.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailOutboxDispatcherTest {
    @Mock EmailOutboxRepository outboxRepository;
    @Mock EmailOutboxClaimService claimService;
    @Mock NotificationRepository notificationRepository;
    @Mock ReminderRepository reminderRepository;
    @Mock UserRepository userRepository;
    @Mock EmailSender emailSender;

    private final Instant now = Instant.parse("2026-10-13T10:00:00Z");
    private EmailOutboxDispatcher dispatcher;
    private EmailOutbox job;
    private Notification notification;
    private Reminder reminder;
    private User user;

    @BeforeEach
    void setUp() {
        dispatcher = new EmailOutboxDispatcher(outboxRepository, claimService, notificationRepository, reminderRepository,
                userRepository, emailSender, Clock.fixed(now, ZoneOffset.UTC), 3, 60, 3600,
                "http://localhost:5173");
        UUID userId = UUID.randomUUID();
        UUID reminderId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        job = new EmailOutbox(notificationId, "user@example.com", now);
        notification = new Notification(userId, reminderId, NotificationType.DUE, now);
        reminder = new Reminder(userId, "Pay rent", "Monthly payment", null, now.plusSeconds(3600), null);
        user = new User("user@example.com", "hash", "User", "Asia/Singapore");
        when(claimService.claimDueJobs()).thenReturn(List.of(job));
        when(notificationRepository.findById(any())).thenReturn(Optional.of(notification));
        when(reminderRepository.findById(any())).thenReturn(Optional.of(reminder));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
    }

    @Test
    void sendsEmailAndMarksJobSent() {
        assertThat(dispatcher.dispatchNow()).isEqualTo(1);

        verify(emailSender).send("user@example.com", "Reminder due: Pay rent", 
                org.mockito.ArgumentMatchers.contains("Monthly payment"),
                org.mockito.ArgumentMatchers.contains("Asia"));
        assertThat(job.getStatus()).isEqualTo(EmailOutboxStatus.SENT);
    }

    @Test
    void retriesDeliveryFailureWithBackoff() {
        doThrow(new RuntimeException("SMTP unavailable")).when(emailSender)
                .send(any(), any(), any(), any());

        dispatcher.dispatchNow();

        assertThat(job.getStatus()).isEqualTo(EmailOutboxStatus.PENDING);
        assertThat(job.getNextAttemptAt()).isEqualTo(now.plusSeconds(60));
        assertThat(job.getLastError()).isEqualTo("SMTP unavailable");
    }
}
