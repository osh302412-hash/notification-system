package com.notification.infrastructure.provider.email

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.ProviderResult
import com.notification.domain.model.User
import com.notification.infrastructure.provider.NotificationProvider
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Mock Email Provider with deterministic failure mode.
 *
 * Failure triggers:
 * - Recipient email contains "fail" -> permanent failure (non-retryable)
 * - Recipient email contains "timeout" -> timeout error (retryable)
 * - Metadata contains "forceFailAttempt" and current attempt matches -> retryable failure
 */
@Component
class MockEmailProvider : NotificationProvider {
    override val channel = NotificationChannel.EMAIL
    override val providerName = "mock-email"

    override fun send(user: User, title: String, body: String, metadata: Map<String, Any>): ProviderResult {
        val startTime = System.currentTimeMillis()

        logger.info { "Mock Email: sending to ${user.email}, title=$title" }

        // Deterministic failure: email contains "fail"
        if (user.email.contains("fail")) {
            val duration = System.currentTimeMillis() - startTime
            logger.warn { "Mock Email: permanent failure for ${user.email}" }
            return ProviderResult(
                success = false,
                provider = providerName,
                errorCode = "INVALID_RECIPIENT",
                errorMessage = "Permanent failure: invalid recipient",
                retryable = false,
                durationMs = duration
            )
        }

        // Deterministic failure: email contains "timeout"
        if (user.email.contains("timeout")) {
            val duration = System.currentTimeMillis() - startTime
            logger.warn { "Mock Email: timeout for ${user.email}" }
            return ProviderResult(
                success = false,
                provider = providerName,
                errorCode = "TIMEOUT",
                errorMessage = "Provider timeout",
                retryable = true,
                durationMs = duration
            )
        }

        // Deterministic failure: attempt-based
        val forceFailAttempt = metadata["forceFailAttempt"]?.toString()?.toIntOrNull()
        val currentAttempt = metadata["attemptNumber"]?.toString()?.toIntOrNull() ?: 1
        if (forceFailAttempt != null && currentAttempt <= forceFailAttempt) {
            val duration = System.currentTimeMillis() - startTime
            logger.warn { "Mock Email: forced failure at attempt $currentAttempt (fail until attempt > $forceFailAttempt)" }
            return ProviderResult(
                success = false,
                provider = providerName,
                errorCode = "TEMPORARY_FAILURE",
                errorMessage = "Temporary provider error at attempt $currentAttempt",
                retryable = true,
                durationMs = duration
            )
        }

        // Success
        val messageId = "email-${UUID.randomUUID()}"
        val duration = System.currentTimeMillis() - startTime
        logger.info { "Mock Email: sent successfully, messageId=$messageId" }

        return ProviderResult(
            success = true,
            provider = providerName,
            messageId = messageId,
            responsePayload = mapOf("messageId" to messageId, "recipient" to user.email),
            durationMs = duration
        )
    }
}
