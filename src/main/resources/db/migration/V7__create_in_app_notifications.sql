CREATE TABLE in_app_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    notification_request_id UUID REFERENCES notification_requests(id),
    title VARCHAR(500) NOT NULL,
    body TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    data JSONB DEFAULT '{}',
    read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_in_app_user ON in_app_notifications(user_id, created_at DESC);
CREATE INDEX idx_in_app_unread ON in_app_notifications(user_id, read) WHERE read = FALSE;
