package com.remindly.email;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_outbox")
public class EmailOutbox {
    @Id @UuidGenerator private UUID id;
    @Column(name = "notification_id", nullable = false, unique = true) private UUID notificationId;
    @Column(name = "recipient_email", nullable = false, length = 320) private String recipientEmail;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private EmailOutboxStatus status;
    @Column(name = "attempt_count", nullable = false) private int attemptCount;
    @Column(name = "next_attempt_at", nullable = false) private Instant nextAttemptAt;
    @Column(name = "processing_started_at") private Instant processingStartedAt;
    @Column(name = "sent_at") private Instant sentAt;
    @Column(name = "last_error", columnDefinition = "TEXT") private String lastError;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected EmailOutbox() {}

    public EmailOutbox(UUID notificationId, String recipientEmail, Instant now) {
        this.notificationId = notificationId;
        this.recipientEmail = recipientEmail;
        this.status = EmailOutboxStatus.PENDING;
        this.nextAttemptAt = now;
    }

    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getNotificationId() { return notificationId; }
    public String getRecipientEmail() { return recipientEmail; }
    public EmailOutboxStatus getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public Instant getProcessingStartedAt() { return processingStartedAt; }
    public String getLastError() { return lastError; }

    public void claim(Instant now) {
        status = EmailOutboxStatus.PROCESSING;
        processingStartedAt = now;
        attemptCount++;
        lastError = null;
    }

    public void markSent(Instant now) {
        status = EmailOutboxStatus.SENT;
        sentAt = now;
        processingStartedAt = null;
        nextAttemptAt = now;
    }

    public void retry(Instant nextAttempt, String error) {
        status = EmailOutboxStatus.PENDING;
        processingStartedAt = null;
        nextAttemptAt = nextAttempt;
        lastError = error;
    }

    public void fail(String error) {
        status = EmailOutboxStatus.FAILED;
        processingStartedAt = null;
        lastError = error;
    }

    public void requeue(Instant now) {
        status = EmailOutboxStatus.PENDING;
        processingStartedAt = null;
        nextAttemptAt = now;
    }
}
