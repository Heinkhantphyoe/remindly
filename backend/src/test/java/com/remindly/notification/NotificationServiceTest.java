package com.remindly.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    void listsOnlyNotificationsReturnedForUser() {
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification(userId, UUID.randomUUID(), NotificationType.DUE,
                Instant.parse("2026-10-13T10:00:00Z"));
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(notificationRepository.findByUserId(userId, pageRequest))
                .thenReturn(new PageImpl<>(List.of(notification)));

        var response = new NotificationService(notificationRepository).list(userId, pageRequest);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().notificationType()).isEqualTo(NotificationType.DUE);
        verify(notificationRepository).findByUserId(userId, pageRequest);
    }

    @Test
    void returnsUnreadCount() {
        UUID userId = UUID.randomUUID();
        when(notificationRepository.countByUserIdAndReadAtIsNull(userId)).thenReturn(3L);

        assertThat(new NotificationService(notificationRepository).unreadCount(userId).count()).isEqualTo(3);
    }

    @Test
    void marksOwnedNotificationAsRead() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification(userId, UUID.randomUUID(), NotificationType.ADVANCE,
                Instant.parse("2026-10-13T10:00:00Z"));
        when(notificationRepository.findByIdAndUserId(notificationId, userId)).thenReturn(Optional.of(notification));

        var response = new NotificationService(notificationRepository).markRead(userId, notificationId);

        assertThat(response.readAt()).isNotNull();
    }

    @Test
    void cannotMarkAnotherUsersNotificationAsRead() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findByIdAndUserId(notificationId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new NotificationService(notificationRepository).markRead(userId, notificationId))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void marksAllNotificationsAsReadForUser() {
        UUID userId = UUID.randomUUID();

        new NotificationService(notificationRepository).markAllRead(userId);

        verify(notificationRepository).markAllRead(any(UUID.class), any(Instant.class));
    }
}
