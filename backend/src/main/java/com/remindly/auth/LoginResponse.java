package com.remindly.auth;

public record LoginResponse(
        String accessToken,
        long expiresIn,
        UserResponse user
) {
}

