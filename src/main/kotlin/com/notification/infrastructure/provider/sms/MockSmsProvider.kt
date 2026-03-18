package com.notification.infrastructure.provider.sms

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.ProviderResult
import com.notification.domain.model.User
import com.notification.infrastructure.provider.NotificationProvider
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Component
class MockSmsProvider : NotificationProvider {
    override val channel = NotificationChannel.SMS
    override val providerName = "mock-sms"

    override fun send(user: User, title: String, body: String, metadata: Map<String, Any>): ProviderResult {
        val startTime = System.currentTimeMillis()

        logger.info { "Mock SMS: sending to ${user.phoneNumber}, body=$body" }

        if (user.phoneNumber.isNullOrBlank()) {
            val duration = System.currentTimeMillis() - startTime
            return ProviderResult(
                success = false,
                provider = providerName,
                errorCode = "INVALID_RECIPIENT",
                errorMessage = "No phone number",
                retryable = false,
                durationMs = duration
            )
        }

        if (user.phoneNumber.contains("0000000")) {
            val duration = System.currentTimeMillis() - startTime
            return ProviderResult(
                success = false,
                provider = providerName,
                errorCode = "TEMPORARY_FAILURE",
                errorMessage = "SMS gateway error",
                retryable = true,
                durationMs = duration
            )
        }

        val forceFailAttempt = metadata["forceFailAttempt"]?.toString()?.toIntOrNull()
        val currentAttempt = metadata["attemptNumber"]?.toString()?.toIntOrNull() ?: 1
        if (forceFailAttempt != null && currentAttempt <= forceFailAttempt) {
            val duration = System.currentTimeMillis() - startTime
            return ProviderResult(
                success = false,
                provider = providerName,
                errorCode = "TEMPORARY_FAILURE",
                errorMessage = "Temporary provider error at attempt $currentAttempt",
                retryable = true,
                durationMs = duration
            )
        }

        val messageId = "sms-${UUID.randomUUID()}"
        val duration = System.currentTimeMillis() - startTime
        logger.info { "Mock SMS: sent successfully, messageId=$messageId" }

        return ProviderResult(
            success = true,
            provider = providerName,
            messageId = messageId,
            responsePayload = mapOf("messageId" to messageId, "recipient" to user.phoneNumber),
            durationMs = duration
        )
    }
}
