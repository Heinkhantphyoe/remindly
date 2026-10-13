package com.remindly.reminder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 80)
    private String category;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "advance_offset_minutes")
    private Integer advanceOffsetMinutes;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Reminder() {
    }

    public Reminder(UUID userId, String title, String description, String category,
                    Instant dueAt, Integer advanceOffsetMinutes) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.dueAt = dueAt;
        this.advanceOffsetMinutes = advanceOffsetMinutes;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public Instant getDueAt() { return dueAt; }
    public Integer getAdvanceOffsetMinutes() { return advanceOffsetMinutes; }
    public boolean isCompleted() { return completed; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(String title, String description, String category,
                       Instant dueAt, Integer advanceOffsetMinutes) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.dueAt = dueAt;
        this.advanceOffsetMinutes = advanceOffsetMinutes;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        this.completedAt = completed ? Instant.now() : null;
    }
}
