#!/bin/bash
# Notification System - cURL 예시 모음
# 사용법: 각 명령을 개별적으로 복사하여 실행하세요.

BASE_URL="http://localhost:8080"

echo "=== 1. 회원가입 완료 알림 ==="
curl -s -X POST "$BASE_URL/api/v1/notifications" \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: corr-welcome-001" \
  -d '{
    "userId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "notificationType": "ACCOUNT",
    "templateCode": "WELCOME",
    "variables": {"userName": "홍길동"},
    "channels": ["EMAIL", "IN_APP"],
    "priority": "NORMAL",
    "idempotencyKey": "welcome-curl-'$(date +%s)'"
  }' | jq .

echo ""
echo "=== 2. 결제 완료 알림 ==="
curl -s -X POST "$BASE_URL/api/v1/notifications" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "notificationType": "TRANSACTION",
    "templateCode": "PAYMENT_COMPLETE",
    "variables": {"userName": "홍길동", "amount": "50000", "orderId": "ORD-001"},
    "channels": ["SMS", "PUSH", "IN_APP"],
    "idempotencyKey": "payment-curl-'$(date +%s)'"
  }' | jq .

echo ""
echo "=== 3. 보안 경고 (HIGH priority) ==="
curl -s -X POST "$BASE_URL/api/v1/notifications" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "notificationType": "SECURITY",
    "templateCode": "SECURITY_ALERT",
    "variables": {"userName": "홍길동", "alertMessage": "새로운 기기에서 로그인"},
    "priority": "HIGH",
    "idempotencyKey": "security-curl-'$(date +%s)'"
  }' | jq .

echo ""
echo "=== 4. In-App 알림 조회 ==="
curl -s "$BASE_URL/api/v1/users/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/in-app-notifications" | jq .

echo ""
echo "=== 5. 사용자 설정 조회 ==="
curl -s "$BASE_URL/api/v1/users/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/preferences" | jq .

echo ""
echo "=== 6. 템플릿 조회 ==="
curl -s "$BASE_URL/api/v1/templates/WELCOME" | jq .

echo ""
echo "=== 7. 템플릿 렌더링 미리보기 ==="
curl -s -X POST "$BASE_URL/api/v1/templates/WELCOME/render-preview" \
  -H "Content-Type: application/json" \
  -d '{"channel": "EMAIL", "variables": {"userName": "테스트"}}' | jq .

echo ""
echo "=== 8. 헬스체크 ==="
curl -s "$BASE_URL/actuator/health" | jq .
