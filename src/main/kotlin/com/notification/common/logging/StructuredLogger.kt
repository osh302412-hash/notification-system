package com.notification.common.logging

import mu.KotlinLogging
import org.slf4j.MDC
import java.util.UUID

private val logger = KotlinLogging.logger {}

object StructuredLogger {

    fun withContext(
        correlationId: String? = null,
        notificationId: UUID? = null,
        channel: String? = null,
        provider: String? = null,
        attemptCount: Int? = null,
        block: () -> Unit
    ) {
        val previousValues = mutableMapOf<String, String?>()
        try {
            correlationId?.let {
                previousValues["correlationId"] = MDC.get("correlationId")
                MDC.put("correlationId", it)
            }
            notificationId?.let {
                previousValues["notificationId"] = MDC.get("notificationId")
                MDC.put("notificationId", it.toString())
            }
            channel?.let {
                previousValues["channel"] = MDC.get("channel")
                MDC.put("channel", it)
            }
            provider?.let {
                previousValues["provider"] = MDC.get("provider")
                MDC.put("provider", it)
            }
            attemptCount?.let {
                previousValues["attemptCount"] = MDC.get("attemptCount")
                MDC.put("attemptCount", it.toString())
            }
            block()
        } finally {
            previousValues.forEach { (key, value) ->
                if (value != null) MDC.put(key, value) else MDC.remove(key)
            }
        }
    }

    fun notificationRequested(requestId: UUID, userId: UUID, type: String, channels: List<String>) {
        logger.info { "Notification requested: requestId=$requestId, userId=$userId, type=$type, channels=$channels" }
    }

    fun notificationProcessing(requestId: UUID) {
        logger.info { "Processing notification: requestId=$requestId" }
    }

    fun deliveryAttempt(deliveryId: UUID, channel: String, provider: String, attempt: Int) {
        logger.info { "Delivery attempt: deliveryId=$deliveryId, channel=$channel, provider=$provider, attempt=$attempt" }
    }

    fun deliverySuccess(deliveryId: UUID, channel: String, provider: String) {
        logger.info { "Delivery success: deliveryId=$deliveryId, channel=$channel, provider=$provider" }
    }

    fun deliveryFailed(deliveryId: UUID, channel: String, provider: String, error: String, retryable: Boolean) {
        logger.warn { "Delivery failed: deliveryId=$deliveryId, channel=$channel, provider=$provider, error=$error, retryable=$retryable" }
    }

    fun deliveryRetryScheduled(deliveryId: UUID, channel: String, attempt: Int, nextRetryAt: String) {
        logger.info { "Retry scheduled: deliveryId=$deliveryId, channel=$channel, attempt=$attempt, nextRetryAt=$nextRetryAt" }
    }

    fun deliveryDeadLettered(deliveryId: UUID, channel: String, reason: String) {
        logger.error { "Dead-lettered: deliveryId=$deliveryId, channel=$channel, reason=$reason" }
    }

    fun preferenceBlocked(userId: UUID, channel: String, reason: String) {
        logger.info { "Blocked by preference: userId=$userId, channel=$channel, reason=$reason" }
    }

    fun quietHoursBlocked(userId: UUID, channel: String) {
        logger.info { "Blocked by quiet hours: userId=$userId, channel=$channel" }
    }

    fun duplicateRequest(idempotencyKey: String) {
        logger.info { "Duplicate request detected: idempotencyKey=$idempotencyKey" }
    }

    fun idempotencyHit(idempotencyKey: String, existingId: UUID) {
        logger.info { "Idempotency hit: key=$idempotencyKey, existingId=$existingId" }
    }
}
