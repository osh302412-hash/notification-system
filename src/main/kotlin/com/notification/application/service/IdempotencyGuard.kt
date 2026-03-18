package com.notification.application.service

import com.notification.common.exception.DuplicateRequestException
import com.notification.common.logging.StructuredLogger
import com.notification.domain.model.IdempotencyRecord
import com.notification.domain.repository.IdempotencyRepository
import com.notification.infrastructure.config.NotificationProperties
import mu.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@Service
class IdempotencyGuard(
    private val idempotencyRepository: IdempotencyRepository,
    private val redisTemplate: StringRedisTemplate,
    private val properties: NotificationProperties
) {
    companion object {
        private const val REDIS_KEY_PREFIX = "idempotency:"
    }

    /**
     * Check and acquire idempotency lock.
     * Returns existing request ID if duplicate, null if new request.
     */
    fun checkAndAcquire(idempotencyKey: String): UUID? {
        // 1. Fast check in Redis
        val redisKey = "$REDIS_KEY_PREFIX$idempotencyKey"
        val existing = redisTemplate.opsForValue().get(redisKey)
        if (existing != null) {
            StructuredLogger.idempotencyHit(idempotencyKey, UUID.fromString(existing))
            return UUID.fromString(existing)
        }

        // 2. Check in DB
        val record = idempotencyRepository.findByKey(idempotencyKey)
        if (record != null && record.notificationRequestId != null) {
            // Cache in Redis for future fast lookups
            redisTemplate.opsForValue().set(
                redisKey,
                record.notificationRequestId.toString(),
                properties.idempotency.ttlHours,
                TimeUnit.HOURS
            )
            StructuredLogger.idempotencyHit(idempotencyKey, record.notificationRequestId)
            return record.notificationRequestId
        }

        // 3. Try to acquire via Redis SET NX
        val acquired = redisTemplate.opsForValue().setIfAbsent(
            redisKey,
            "PENDING",
            properties.idempotency.ttlHours,
            TimeUnit.HOURS
        ) ?: false

        if (!acquired) {
            // Another thread/instance acquired it
            val currentValue = redisTemplate.opsForValue().get(redisKey)
            if (currentValue != null && currentValue != "PENDING") {
                return UUID.fromString(currentValue)
            }
            // Still PENDING - concurrent request in progress
            throw DuplicateRequestException(idempotencyKey, UUID.randomUUID())
        }

        return null // New request, lock acquired
    }

    fun confirm(idempotencyKey: String, requestId: UUID) {
        val redisKey = "$REDIS_KEY_PREFIX$idempotencyKey"

        // Update Redis
        redisTemplate.opsForValue().set(
            redisKey,
            requestId.toString(),
            properties.idempotency.ttlHours,
            TimeUnit.HOURS
        )

        // Save to DB for durability
        idempotencyRepository.save(
            IdempotencyRecord(
                idempotencyKey = idempotencyKey,
                notificationRequestId = requestId,
                status = "CONFIRMED",
                expiresAt = ZonedDateTime.now().plusHours(properties.idempotency.ttlHours)
            )
        )
    }

    fun release(idempotencyKey: String) {
        val redisKey = "$REDIS_KEY_PREFIX$idempotencyKey"
        redisTemplate.delete(redisKey)
    }
}
