CREATE TABLE email_outbox (
    id UUID PRIMARY KEY,
    notification_id UUID NOT NULL,
    recipient_email VARCHAR(320) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processing_started_at TIMESTAMPTZ,
    sent_at TIMESTAMPTZ,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT email_outbox_notification_unique UNIQUE (notification_id),
    CONSTRAINT email_outbox_notification_fk FOREIGN KEY (notification_id)
        REFERENCES notifications (id) ON DELETE CASCADE,
    CONSTRAINT email_outbox_status_valid CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED')),
    CONSTRAINT email_outbox_attempts_nonnegative CHECK (attempt_count >= 0)
);

CREATE INDEX email_outbox_due_idx ON email_outbox (status, next_attempt_at);
CREATE INDEX email_outbox_processing_idx ON email_outbox (status, processing_started_at);
