package com.remindly.auth;

import com.remindly.user.User;
import com.remindly.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;

@Service
public class EmailVerificationService {
    private final UserRepository users;
    private final EmailVerificationTokenRepository tokens;
    private final AuthEmailService emails;

    public EmailVerificationService(UserRepository users, EmailVerificationTokenRepository tokens, AuthEmailService emails) {
        this.users = users; this.tokens = tokens; this.emails = emails;
    }

    @Transactional
    public void issue(User user) {
        tokens.deleteByUserId(user.getId());
        String raw = AuthTokenHasher.generate();
        tokens.save(new EmailVerificationToken(user.getId(), AuthTokenHasher.hash(raw), Instant.now().plus(Duration.ofHours(24))));
        emails.sendVerification(user.getEmail(), raw);
    }

    @Transactional
    public void verify(String rawToken) {
        Instant now = Instant.now();
        EmailVerificationToken token = tokens.findByTokenHash(AuthTokenHasher.hash(rawToken))
                .filter(value -> value.isUsable(now)).orElseThrow(InvalidAuthTokenException::new);
        User user = users.findById(token.getUserId()).orElseThrow(InvalidAuthTokenException::new);
        user.verifyEmail(); token.markUsed(now);
    }
}
