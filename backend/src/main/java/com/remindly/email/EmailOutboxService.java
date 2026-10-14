package com.remindly.email;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class EmailOutboxService {
    private final EmailOutboxRepository repository;

    public EmailOutboxService(EmailOutboxRepository repository) { this.repository = repository; }

    @Transactional
    public int enqueue(UUID notificationId, String recipientEmail, Instant now) {
        return repository.insertIfAbsent(UUID.randomUUID(), notificationId, recipientEmail, now);
    }
}
