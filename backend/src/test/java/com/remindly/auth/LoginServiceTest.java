package com.remindly.auth;

import com.remindly.user.User;
import com.remindly.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void logsInWithValidCredentials() {
        User user = new User("user@example.com", passwordEncoder.encode("password123"), "User", "UTC");
        LoginService service = new LoginService(userRepository, passwordEncoder, jwtTokenService, refreshTokenService);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenService.createAccessToken(user)).thenReturn("access-token");
        when(jwtTokenService.getAccessTokenTtlSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(user)).thenReturn(new RefreshTokenService.IssuedRefreshToken("refresh-token", null));

        LoginResponse response = service.login(new LoginRequest(" USER@Example.com ", "password123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.expiresIn()).isEqualTo(900);
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().email()).isEqualTo("user@example.com");
    }

    @Test
    void rejectsUnknownEmail() {
        LoginService service = new LoginService(userRepository, passwordEncoder, jwtTokenService, refreshTokenService);
        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("unknown@example.com", "password123")))
                .isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void rejectsIncorrectPassword() {
        User user = new User("user@example.com", passwordEncoder.encode("password123"), "User", "UTC");
        LoginService service = new LoginService(userRepository, passwordEncoder, jwtTokenService, refreshTokenService);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new LoginRequest("user@example.com", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(jwtTokenService);
    }
}

