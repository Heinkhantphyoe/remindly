package com.remindly.auth;

import com.remindly.user.User;
import com.remindly.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    public LoginService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!user.isEmailVerified()) {
            throw new InvalidCredentialsException();
        }

        return createLoginResponse(user, refreshTokenService.issue(user).rawToken());
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(request.refreshToken());
        return createLoginResponse(rotated.user(), rotated.rawToken());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private LoginResponse createLoginResponse(User user, String refreshToken) {
        return new LoginResponse(
                jwtTokenService.createAccessToken(user),
                refreshToken,
                jwtTokenService.getAccessTokenTtlSeconds(),
                UserResponse.from(user)
        );
    }
}

