package com.notification.application.orchestrator

import com.notification.application.service.*
import com.notification.common.logging.StructuredLogger
import com.notification.domain.model.*
import com.notification.domain.repository.*
import com.notification.infrastructure.provider.ProviderRegistry
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class DeliveryProcessor(
    private val userRepository: UserRepository,
    private val requestRepository: NotificationRequestRepository,
    private val deliveryRepository: NotificationDeliveryRepository,
    private val attemptRepository: NotificationDeliveryAttemptRepository,
    private val inAppNotificationRepository: InAppNotificationRepository,
    private val templateRenderer: TemplateRenderer,
    private val providerRegistry: ProviderRegistry,
    private val retryPolicyService: RetryPolicyService,
    private val deduplicationService: DeduplicationService
) {
    @Transactional
    fun processDelivery(delivery: NotificationDelivery) {
        val request = requestRepository.findById(delivery.notificationRequestId)
            ?: run {
                logger.error { "Request not found for delivery ${delivery.id}" }
                return
            }

        val user = userRepository.findById(request.userId)
            ?: run {
                logger.error { "User not found: ${request.userId}" }
                deliveryRepository.markDeadLetter(delivery.id!!, "USER_NOT_FOUND", "User not found")
                return
            }

        val correlationId = request.correlationId ?: delivery.id.toString()

        StructuredLogger.withContext(
            correlationId = correlationId,
            notificationId = request.id,
            channel = delivery.channel.name,
            attemptCount = delivery.attemptCount + 1
        ) {
            processDeliveryInternal(delivery, request, user)
        }
    }

    private fun processDeliveryInternal(
        delivery: NotificationDelivery,
        request: NotificationRequest,
        user: User
    ) {
        val channel = delivery.channel
        val attemptNumber = delivery.attemptCount + 1

        // Handle IN_APP differently - direct DB store
        if (channel == NotificationChannel.IN_APP) {
            processInAppDelivery(delivery, request, user)
            return
        }

        // Render template
        val rendered = try {
            templateRenderer.render(request.templateCode, channel, request.variables)
        } catch (e: Exception) {
            logger.error(e) { "Template rendering failed for ${request.templateCode}, channel=$channel" }
            deliveryRepository.markDeadLetter(delivery.id!!, "TEMPLATE_ERROR", e.message)
            recordAttempt(delivery.id!!, attemptNumber, "none", DeliveryAttemptStatus.ERROR,
                errorCode = "TEMPLATE_ERROR", errorMessage = e.message)
            updateRequestStatus(request.id!!)
            return
        }

        // Get provider
        val provider = try {
            providerRegistry.getProvider(channel)
        } catch (e: Exception) {
            logger.error(e) { "No provider for channel $channel" }
            deliveryRepository.markDeadLetter(delivery.id!!, "NO_PROVIDER", e.message)
            recordAttempt(delivery.id!!, attemptNumber, "none", DeliveryAttemptStatus.ERROR,
                errorCode = "NO_PROVIDER", errorMessage = e.message)
            updateRequestStatus(request.id!!)
            return
        }

        StructuredLogger.deliveryAttempt(delivery.id!!, channel.name, provider.providerName, attemptNumber)

        // Send
        val metadata = mapOf(
            "attemptNumber" to attemptNumber,
            "notificationRequestId" to request.id.toString(),
            "notificationType" to request.notificationType.name,
            "templateCode" to request.templateCode
        )

        val result = provider.send(user, rendered.title, rendered.body, metadata)

        // Record attempt
        recordAttempt(
            deliveryId = delivery.id!!,
            attemptNumber = attemptNumber,
            provider = result.provider,
            status = if (result.success) DeliveryAttemptStatus.SUCCESS else DeliveryAttemptStatus.FAILED,
            requestPayload = mapOf("title" to rendered.title, "body" to rendered.body),
            responsePayload = result.responsePayload,
            errorCode = result.errorCode,
            errorMessage = result.errorMessage,
            durationMs = result.durationMs
        )

        if (result.success) {
            StructuredLogger.deliverySuccess(delivery.id!!, channel.name, provider.providerName)
            deliveryRepository.markCompleted(delivery.id!!, provider.providerName)
            delivery.dedupKey?.let { deduplicationService.markSent(it) }
        } else {
            StructuredLogger.deliveryFailed(delivery.id!!, channel.name, provider.providerName,
                result.errorMessage ?: "unknown", result.retryable)
            handleFailure(delivery, result)
        }

        updateRequestStatus(request.id!!)
    }

    private fun processInAppDelivery(
        delivery: NotificationDelivery,
        request: NotificationRequest,
        user: User
    ) {
        try {
            val rendered = templateRenderer.render(request.templateCode, NotificationChannel.IN_APP, request.variables)

            inAppNotificationRepository.save(
                InAppNotification(
                    userId = user.id,
                    notificationRequestId = request.id,
                    title = rendered.title,
                    body = rendered.body,
                    notificationType = request.notificationType,
                    data = request.variables.toMap()
                )
            )

            deliveryRepository.markCompleted(delivery.id!!, "in-app-store")
            recordAttempt(delivery.id!!, 1, "in-app-store", DeliveryAttemptStatus.SUCCESS)
            StructuredLogger.deliverySuccess(delivery.id!!, "IN_APP", "in-app-store")
        } catch (e: Exception) {
            logger.error(e) { "Failed to process in-app delivery" }
            deliveryRepository.markDeadLetter(delivery.id!!, "IN_APP_ERROR", e.message)
            recordAttempt(delivery.id!!, 1, "in-app-store", DeliveryAttemptStatus.ERROR,
                errorCode = "IN_APP_ERROR", errorMessage = e.message)
        }
        updateRequestStatus(request.id!!)
    }

    private fun handleFailure(delivery: NotificationDelivery, result: ProviderResult) {
        val canRetry = result.retryable && retryPolicyService.isRetryableError(result.errorCode)
            && retryPolicyService.shouldRetry(delivery.attemptCount + 1, delivery.maxAttempts)

        if (canRetry) {
            val nextRetryAt = retryPolicyService.calculateNextRetryTime(delivery.attemptCount + 1)
            deliveryRepository.incrementAttemptAndScheduleRetry(delivery.id!!, nextRetryAt)
            StructuredLogger.deliveryRetryScheduled(
                delivery.id!!, delivery.channel.name,
                delivery.attemptCount + 1, nextRetryAt.toString()
            )
        } else {
            deliveryRepository.incrementAttemptAndScheduleRetry(delivery.id!!, null)
            deliveryRepository.markDeadLetter(delivery.id!!, result.errorCode, result.errorMessage)
            StructuredLogger.deliveryDeadLettered(
                delivery.id!!, delivery.channel.name,
                "Retry exhausted or non-retryable: ${result.errorCode}"
            )
        }
    }

    private fun recordAttempt(
        deliveryId: UUID,
        attemptNumber: Int,
        provider: String,
        status: DeliveryAttemptStatus,
        requestPayload: Map<String, Any>? = null,
        responsePayload: Map<String, Any>? = null,
        errorCode: String? = null,
        errorMessage: String? = null,
        durationMs: Long? = null
    ) {
        attemptRepository.save(
            NotificationDeliveryAttempt(
                deliveryId = deliveryId,
                attemptNumber = attemptNumber,
                provider = provider,
                status = status,
                requestPayload = requestPayload,
                responsePayload = responsePayload,
                errorCode = errorCode,
                errorMessage = errorMessage,
                durationMs = durationMs
            )
        )
    }

    fun updateRequestStatus(requestId: UUID) {
        val deliveries = deliveryRepository.findByNotificationRequestId(requestId)
        if (deliveries.isEmpty()) return

        val allTerminal = deliveries.all { it.status in listOf(DeliveryStatus.SENT, DeliveryStatus.DELIVERED, DeliveryStatus.FAILED, DeliveryStatus.DEAD_LETTER, DeliveryStatus.SKIPPED) }
        if (!allTerminal) return

        val hasSuccess = deliveries.any { it.status == DeliveryStatus.SENT || it.status == DeliveryStatus.DELIVERED }
        val hasFailure = deliveries.any { it.status == DeliveryStatus.FAILED || it.status == DeliveryStatus.DEAD_LETTER }

        val newStatus = when {
            hasSuccess && hasFailure -> NotificationRequestStatus.PARTIALLY_COMPLETED
            hasSuccess -> NotificationRequestStatus.COMPLETED
            else -> NotificationRequestStatus.FAILED
        }

        requestRepository.updateStatus(requestId, newStatus)
    }
}
