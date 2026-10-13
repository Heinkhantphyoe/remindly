package com.remindly.auth;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String email) {
}

