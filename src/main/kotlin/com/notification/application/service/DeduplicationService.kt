package com.notification.application.service

import com.notification.infrastructure.config.NotificationProperties
import mu.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@Service
class DeduplicationService(
    private val redisTemplate: StringRedisTemplate,
    private val properties: NotificationProperties
) {
    companion object {
        private const val DEDUP_KEY_PREFIX = "dedup:"
    }

    /**
     * Generate dedup key for a specific channel delivery.
     * Key = userId:notificationType:channel:templateCode within time window.
     */
    fun generateDedupKey(userId: UUID, notificationType: String, channel: String, templateCode: String): String {
        return "$userId:$notificationType:$channel:$templateCode"
    }

    /**
     * Check if this delivery is a duplicate within the dedup window.
     * Returns true if duplicate (should be skipped).
     */
    fun isDuplicate(dedupKey: String): Boolean {
        val redisKey = "$DEDUP_KEY_PREFIX$dedupKey"
        val existing = redisTemplate.opsForValue().get(redisKey)
        if (existing != null) {
            logger.info { "Dedup hit: key=$dedupKey" }
            return true
        }
        return false
    }

    /**
     * Mark this delivery as sent to prevent future duplicates within window.
     */
    fun markSent(dedupKey: String) {
        val redisKey = "$DEDUP_KEY_PREFIX$dedupKey"
        redisTemplate.opsForValue().set(
            redisKey,
            "1",
            properties.deduplication.windowMinutes,
            TimeUnit.MINUTES
        )
    }
}
