# Notification System Load Test Results

## Environment
- Spring Boot 3 + Kotlin + PostgreSQL + Redis
- Docker Compose (local)
- k6 v0.55.0

## Throughput Test (100 VUs, 1m 40s ramp)

| Metric              | Value    |
|---------------------|----------|
| Peak RPS            | ~277 req/s |
| p95 latency         | 244 ms   |
| p99 latency         | 303 ms   |
| Success rate        | 96%      |
| http_req_failed     | 4%       |

## Idempotency Test (10 VUs, 30s)

- **Bug discovered**: under concurrent load, idempotency records can become orphaned
- Root cause: non-atomic handling between idempotency record creation and notification request persistence — if the notification request fails after the idempotency record is committed, subsequent requests with the same key get a 500 error ("Idempotency record references non-existent request")
- Interview point: demonstrates awareness of distributed systems failure modes in idempotency patterns

## Key Findings

1. **277 RPS** with async notification pipeline (preference resolution → delivery routing → multi-channel dispatch)
2. **p95 < 250ms** under 100 VU concurrent load
3. **Idempotency race condition** exposed under load — requires atomic upsert or saga-style rollback on failure
4. **Quiet hours enforcement** correctly skips channels (EMAIL/PUSH) outside preferred hours, visible in delivery status logs
