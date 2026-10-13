package com.remindly.auth;

import com.remindly.user.User;
import com.remindly.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void registersUserWithNormalizedEmailAndHashedPassword() {
        RegistrationService service = new RegistrationService(userRepository, passwordEncoder);
        RegisterRequest request = new RegisterRequest("  USER@Example.com ", "password123", " User ", "Asia/Singapore");
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = service.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(savedUser.getDisplayName()).isEqualTo("User");
        assertThat(passwordEncoder.matches("password123", savedUser.getPasswordHash())).isTrue();
        assertThat(response.email()).isEqualTo("user@example.com");
    }

    @Test
    void rejectsDuplicateEmail() {
        RegistrationService service = new RegistrationService(userRepository, passwordEncoder);
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(
                new RegisterRequest("user@example.com", "password123", "User", "UTC")
        )).isInstanceOf(EmailAlreadyUsedException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsInvalidTimezone() {
        RegistrationService service = new RegistrationService(userRepository, passwordEncoder);

        assertThatThrownBy(() -> service.register(
                new RegisterRequest("user@example.com", "password123", "User", "Not/A_Timezone")
        )).isInstanceOf(InvalidTimezoneException.class);
        verify(userRepository, never()).save(any());
    }
}

