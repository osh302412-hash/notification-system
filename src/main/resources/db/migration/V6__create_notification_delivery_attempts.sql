CREATE TABLE notification_delivery_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL REFERENCES notification_deliveries(id),
    attempt_number INT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    request_payload JSONB,
    response_payload JSONB,
    error_code VARCHAR(100),
    error_message TEXT,
    duration_ms BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attempts_delivery ON notification_delivery_attempts(delivery_id);
