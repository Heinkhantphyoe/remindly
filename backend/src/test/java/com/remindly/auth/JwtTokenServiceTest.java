package com.remindly.auth;

import com.remindly.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenServiceTest {

    private static final String SECRET = "remindly-test-secret-key-with-at-least-32-bytes";

    @Test
    void createsSignedTokenWithExpectedClaimsAndExpiration() {
        JwtTokenService service = new JwtTokenService(SECRET, 900);
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("user@example.com");

        String token = service.createAccessToken(user);
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo("user@example.com");
        assertThat(claims.getExpiration()).isAfter(new Date());
        assertThat(service.getAccessTokenTtlSeconds()).isEqualTo(900);
    }
}

