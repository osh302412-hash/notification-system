-- Seed users
INSERT INTO users (id, email, phone_number, device_token, name, timezone) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'user1@example.com', '+821012345678', 'device-token-001', '홍길동', 'Asia/Seoul'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'user2@example.com', '+821087654321', 'device-token-002', '김철수', 'Asia/Seoul'),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'fail@example.com', '+820000000000', 'device-token-fail', '실패테스트', 'Asia/Seoul');

-- Seed notification templates
INSERT INTO notification_templates (code, version, notification_type, channel, title_template, body_template) VALUES
('WELCOME', 1, 'ACCOUNT', 'EMAIL', '{{userName}}님, 환영합니다!', '안녕하세요 {{userName}}님, 회원가입을 축하합니다. 서비스를 자유롭게 이용해주세요.'),
('WELCOME', 1, 'ACCOUNT', 'IN_APP', '환영합니다!', '{{userName}}님, 회원가입이 완료되었습니다.'),
('PAYMENT_COMPLETE', 1, 'TRANSACTION', 'SMS', '결제 완료', '{{userName}}님, {{amount}}원 결제가 완료되었습니다. 주문번호: {{orderId}}'),
('PAYMENT_COMPLETE', 1, 'TRANSACTION', 'PUSH', '결제 완료 알림', '{{amount}}원 결제가 완료되었습니다.'),
('PAYMENT_COMPLETE', 1, 'TRANSACTION', 'IN_APP', '결제 완료', '{{userName}}님의 {{amount}}원 결제가 완료되었습니다. 주문번호: {{orderId}}'),
('SECURITY_ALERT', 1, 'SECURITY', 'EMAIL', '보안 경고', '{{userName}}님, {{alertMessage}} 본인이 아닌 경우 즉시 비밀번호를 변경해주세요.'),
('SECURITY_ALERT', 1, 'SECURITY', 'SMS', '보안 경고', '[보안경고] {{alertMessage}}'),
('SECURITY_ALERT', 1, 'SECURITY', 'PUSH', '보안 경고', '{{alertMessage}}'),
('SECURITY_ALERT', 1, 'SECURITY', 'IN_APP', '보안 경고', '{{alertMessage}} 본인이 아닌 경우 즉시 비밀번호를 변경해주세요.'),
('MARKETING_PROMO', 1, 'MARKETING', 'EMAIL', '{{promoTitle}}', '{{userName}}님을 위한 특별 혜택! {{promoDescription}}'),
('MARKETING_PROMO', 1, 'MARKETING', 'PUSH', '특별 혜택', '{{promoDescription}}'),
('MARKETING_PROMO', 1, 'MARKETING', 'IN_APP', '{{promoTitle}}', '{{promoDescription}}');

-- Seed user preferences
INSERT INTO user_notification_preferences (user_id, notification_type, channel, enabled, quiet_hours_enabled, quiet_hours_start, quiet_hours_end) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ACCOUNT', 'EMAIL', true, false, null, null),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ACCOUNT', 'IN_APP', true, false, null, null),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'TRANSACTION', 'SMS', true, false, null, null),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'TRANSACTION', 'PUSH', true, false, null, null),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'TRANSACTION', 'IN_APP', true, false, null, null),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'SECURITY', 'EMAIL', true, false, null, null),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'SECURITY', 'SMS', true, false, null, null),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'SECURITY', 'PUSH', true, false, null, null),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'SECURITY', 'IN_APP', true, false, null, null),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'MARKETING', 'EMAIL', true, true, '22:00', '08:00'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'MARKETING', 'PUSH', true, true, '22:00', '08:00'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'MARKETING', 'IN_APP', true, false, null, null),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'ACCOUNT', 'EMAIL', true, false, null, null),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'ACCOUNT', 'IN_APP', true, false, null, null),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'MARKETING', 'EMAIL', false, false, null, null),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'MARKETING', 'PUSH', false, false, null, null);
