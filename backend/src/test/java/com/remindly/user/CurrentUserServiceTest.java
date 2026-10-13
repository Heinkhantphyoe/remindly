package com.remindly.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void returnsSafeCurrentUserResponse() {
        UUID userId = UUID.randomUUID();
        User user = new User("user@example.com", "secret-hash", "User", "UTC");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var response = new CurrentUserService(userRepository).getCurrentUser(userId);

        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.displayName()).isEqualTo("User");
        assertThat(response).hasFieldOrPropertyWithValue("timezone", "UTC");
    }

    @Test
    void rejectsMissingCurrentUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new CurrentUserService(userRepository).getCurrentUser(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updatesValidTimezone() {
        UUID userId = UUID.randomUUID();
        User user = new User("user@example.com", "secret-hash", "User", "UTC");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var response = new CurrentUserService(userRepository).updateTimezone(userId, " Asia/Singapore ");

        assertThat(response.timezone()).isEqualTo("Asia/Singapore");
        assertThat(user.getTimezone()).isEqualTo("Asia/Singapore");
    }

    @Test
    void rejectsInvalidTimezoneUpdate() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> new CurrentUserService(userRepository).updateTimezone(userId, "Not/A_Timezone"))
                .isInstanceOf(com.remindly.auth.InvalidTimezoneException.class);
    }
}

