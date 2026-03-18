package com.notification.unit.retry

import com.notification.application.service.RetryPolicyService
import com.notification.infrastructure.config.NotificationProperties
import com.notification.infrastructure.config.RetryProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class RetryPolicyServiceTest {
    private val properties = NotificationProperties().apply {
        retry = RetryProperties().apply {
            maxAttempts = 3
            initialDelayMs = 1000
            multiplier = 2.0
            maxDelayMs = 30000
        }
    }
    private val service = RetryPolicyService(properties)

    @Test
    fun `should allow retry when attempts not exhausted`() {
        assertTrue(service.shouldRetry(1, 3))
        assertTrue(service.shouldRetry(2, 3))
    }

    @Test
    fun `should not allow retry when attempts exhausted`() {
        assertFalse(service.shouldRetry(3, 3))
        assertFalse(service.shouldRetry(4, 3))
    }

    @Test
    fun `should calculate next retry time with exponential backoff`() {
        val before = ZonedDateTime.now()
        val nextRetry = service.calculateNextRetryTime(1)
        assertTrue(nextRetry.isAfter(before))
    }

    @Test
    fun `should return max attempts from config`() {
        assertEquals(3, service.getMaxAttempts())
    }

    @Test
    fun `should identify retryable errors`() {
        assertTrue(service.isRetryableError(null))
        assertTrue(service.isRetryableError("TIMEOUT"))
        assertTrue(service.isRetryableError("TEMPORARY_FAILURE"))
        assertTrue(service.isRetryableError("CONNECTION_ERROR"))
    }

    @Test
    fun `should identify non-retryable errors`() {
        assertFalse(service.isRetryableError("INVALID_RECIPIENT"))
        assertFalse(service.isRetryableError("INVALID_TEMPLATE"))
        assertFalse(service.isRetryableError("UNSUPPORTED_CHANNEL"))
        assertFalse(service.isRetryableError("PERMANENT_FAILURE"))
        assertFalse(service.isRetryableError("INVALID_PAYLOAD"))
    }
}
