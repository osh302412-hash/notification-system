package com.notification.application.service

import com.notification.infrastructure.config.NotificationProperties
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import kotlin.math.min
import kotlin.math.pow

@Service
class RetryPolicyService(
    private val properties: NotificationProperties
) {
    fun shouldRetry(attemptCount: Int, maxAttempts: Int): Boolean {
        return attemptCount < maxAttempts
    }

    fun calculateNextRetryTime(attemptCount: Int): ZonedDateTime {
        val delayMs = min(
            (properties.retry.initialDelayMs * properties.retry.multiplier.pow(attemptCount.toDouble())).toLong(),
            properties.retry.maxDelayMs
        )
        return ZonedDateTime.now().plusNanos(delayMs * 1_000_000)
    }

    fun getMaxAttempts(): Int = properties.retry.maxAttempts

    fun isRetryableError(errorCode: String?): Boolean {
        if (errorCode == null) return true
        val nonRetryableErrors = setOf(
            "INVALID_RECIPIENT",
            "INVALID_TEMPLATE",
            "UNSUPPORTED_CHANNEL",
            "PERMANENT_FAILURE",
            "INVALID_PAYLOAD"
        )
        return errorCode !in nonRetryableErrors
    }
}
