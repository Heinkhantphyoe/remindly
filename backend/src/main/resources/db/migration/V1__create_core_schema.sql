CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_email_not_blank CHECK (length(trim(email)) > 0),
    CONSTRAINT users_display_name_not_blank CHECK (length(trim(display_name)) > 0),
    CONSTRAINT users_timezone_not_blank CHECK (length(trim(timezone)) > 0)
);

CREATE UNIQUE INDEX users_email_lower_unique_idx ON users (lower(email));

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT refresh_tokens_token_hash_unique UNIQUE (token_hash),
    CONSTRAINT refresh_tokens_user_fk
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX refresh_tokens_user_idx ON refresh_tokens (user_id);
CREATE INDEX refresh_tokens_active_idx ON refresh_tokens (expires_at, revoked_at);

CREATE TABLE reminders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(80),
    due_at TIMESTAMPTZ NOT NULL,
    advance_offset_minutes INTEGER,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT reminders_user_fk
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT reminders_title_not_blank CHECK (length(trim(title)) > 0),
    CONSTRAINT reminders_advance_offset_positive
        CHECK (advance_offset_minutes IS NULL OR advance_offset_minutes > 0),
    CONSTRAINT reminders_completion_consistent
        CHECK ((completed = FALSE AND completed_at IS NULL) OR (completed = TRUE AND completed_at IS NOT NULL))
);

CREATE INDEX reminders_user_due_at_idx ON reminders (user_id, due_at);
CREATE INDEX reminders_user_completed_idx ON reminders (user_id, completed);
CREATE INDEX reminders_user_category_idx ON reminders (user_id, category);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    reminder_id UUID NOT NULL,
    notification_type VARCHAR(20) NOT NULL,
    scheduled_for TIMESTAMPTZ NOT NULL,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT notifications_user_fk
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT notifications_reminder_fk
        FOREIGN KEY (reminder_id) REFERENCES reminders (id) ON DELETE CASCADE,
    CONSTRAINT notifications_type_valid
        CHECK (notification_type IN ('ADVANCE', 'DUE')),
    CONSTRAINT notifications_reminder_type_unique
        UNIQUE (reminder_id, notification_type)
);

CREATE INDEX notifications_user_created_at_idx ON notifications (user_id, created_at DESC);
CREATE INDEX notifications_user_unread_idx ON notifications (user_id, read_at);
CREATE INDEX notifications_scheduled_for_idx ON notifications (scheduled_for);

