package com.remindly.auth;

import com.remindly.user.User;
import com.remindly.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;

@Service
public class PasswordResetService {
    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final AuthEmailService emails;
    private final RefreshTokenRepository refreshTokens;

    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens, PasswordEncoder passwordEncoder, AuthEmailService emails, RefreshTokenRepository refreshTokens) {
        this.users = users; this.tokens = tokens; this.passwordEncoder = passwordEncoder; this.emails = emails; this.refreshTokens = refreshTokens;
    }

    @Transactional
    public void request(String email) {
        users.findByEmailIgnoreCase(email.trim().toLowerCase()).ifPresent(user -> {
            tokens.deleteByUserId(user.getId());
            String raw = AuthTokenHasher.generate();
            tokens.save(new PasswordResetToken(user.getId(), AuthTokenHasher.hash(raw), Instant.now().plus(Duration.ofHours(1))));
            emails.sendPasswordReset(user.getEmail(), raw);
        });
    }

    @Transactional
    public void reset(String rawToken, String password) {
        Instant now = Instant.now();
        PasswordResetToken token = tokens.findByTokenHash(AuthTokenHasher.hash(rawToken))
                .filter(value -> value.isUsable(now)).orElseThrow(InvalidAuthTokenException::new);
        User user = users.findById(token.getUserId()).orElseThrow(InvalidAuthTokenException::new);
        user.updatePassword(passwordEncoder.encode(password));
        refreshTokens.deleteByUser_Id(user.getId());
        token.markUsed(now);
    }
}
