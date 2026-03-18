CREATE TABLE notification_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    notification_type VARCHAR(50) NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    variables JSONB DEFAULT '{}',
    requested_channels TEXT[] NOT NULL,
    resolved_channels TEXT[],
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    scheduled_at TIMESTAMP WITH TIME ZONE,
    preference_snapshot JSONB,
    correlation_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notification_requests_status ON notification_requests(status);
CREATE INDEX idx_notification_requests_user ON notification_requests(user_id);
CREATE INDEX idx_notification_requests_idempotency ON notification_requests(idempotency_key);
CREATE INDEX idx_notification_requests_scheduled ON notification_requests(scheduled_at) WHERE scheduled_at IS NOT NULL AND status = 'PENDING';
