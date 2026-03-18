CREATE TABLE notification_deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_request_id UUID NOT NULL REFERENCES notification_requests(id),
    channel VARCHAR(20) NOT NULL,
    provider VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    dedup_key VARCHAR(255),
    attempt_count INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 3,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    last_attempted_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    error_code VARCHAR(100),
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(notification_request_id, channel)
);

CREATE INDEX idx_deliveries_status ON notification_deliveries(status);
CREATE INDEX idx_deliveries_retry ON notification_deliveries(status, next_retry_at) WHERE status = 'RETRY_PENDING';
CREATE INDEX idx_deliveries_request ON notification_deliveries(notification_request_id);
