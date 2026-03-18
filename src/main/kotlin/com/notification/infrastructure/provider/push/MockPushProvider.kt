package com.notification.infrastructure.provider.push

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.ProviderResult
import com.notification.domain.model.User
import com.notification.infrastructure.provider.NotificationProvider
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Component
class MockPushProvider : NotificationProvider {
    override val channel = NotificationChannel.PUSH
    override val providerName = "mock-push"

    override fun send(user: User, title: String, body: String, metadata: Map<String, Any>): ProviderResult {
        val startTime = System.currentTimeMillis()

        logger.info { "Mock Push: sending to device=${user.deviceToken}, title=$title" }

        if (user.deviceToken.isNullOrBlank()) {
            val duration = System.currentTimeMillis() - startTime
            return ProviderResult(
                success = false,
                provider = providerName,
                errorCode = "INVALID_RECIPIENT",
                errorMessage = "No device token",
                retryable = false,
                durationMs = duration
            )
        }

        if (user.deviceToken.contains("fail")) {
            val duration = System.currentTimeMillis() - startTime
            return ProviderResult(
                success = false,
                provider = providerName,
                errorCode = "DEVICE_NOT_REGISTERED",
                errorMessage = "Device token invalid",
                retryable = false,
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

        val messageId = "push-${UUID.randomUUID()}"
        val duration = System.currentTimeMillis() - startTime
        logger.info { "Mock Push: sent successfully, messageId=$messageId" }

        return ProviderResult(
            success = true,
            provider = providerName,
            messageId = messageId,
            responsePayload = mapOf("messageId" to messageId, "deviceToken" to user.deviceToken),
            durationMs = duration
        )
    }
}
