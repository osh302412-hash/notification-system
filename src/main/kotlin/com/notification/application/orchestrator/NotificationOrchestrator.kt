package com.notification.application.orchestrator

import com.notification.application.service.*
import com.notification.common.exception.*
import com.notification.common.logging.StructuredLogger
import com.notification.domain.model.*
import com.notification.domain.repository.*
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class NotificationOrchestrator(
    private val userRepository: UserRepository,
    private val requestRepository: NotificationRequestRepository,
    private val deliveryRepository: NotificationDeliveryRepository,
    private val channelRouter: ChannelRouter,
    private val preferenceResolver: PreferenceResolver,
    private val idempotencyGuard: IdempotencyGuard,
    private val deduplicationService: DeduplicationService,
    private val retryPolicyService: RetryPolicyService
) {
    @Transactional
    fun createNotification(
        userId: UUID,
        notificationType: NotificationType,
        templateCode: String,
        variables: Map<String, String>,
        channels: List<NotificationChannel>?,
        priority: NotificationPriority,
        idempotencyKey: String,
        scheduledAt: java.time.ZonedDateTime? = null,
        correlationId: String? = null
    ): NotificationRequest {
        // 1. Idempotency check
        val existingRequestId = idempotencyGuard.checkAndAcquire(idempotencyKey)
        if (existingRequestId != null) {
            StructuredLogger.duplicateRequest(idempotencyKey)
            val existing = requestRepository.findById(existingRequestId)
                ?: throw NotificationException("Idempotency record references non-existent request: $existingRequestId")
            return existing
        }

        try {
            // 2. Validate user
            val user = userRepository.findById(userId)
                ?: throw UserNotFoundException(userId)

            // 3. Resolve channels
            val resolvedChannels = channelRouter.resolveChannels(notificationType, channels)

            // 4. Check preferences per channel
            val channelResolutions = preferenceResolver.resolveChannels(
                userId, notificationType, resolvedChannels, priority, user.timezone
            )

            val allowedChannels = channelResolutions.filter { it.allowed }.map { it.channel }
            val blockedChannels = channelResolutions.filter { !it.allowed }

            blockedChannels.forEach {
                logger.info { "Channel ${it.channel} blocked for user $userId: ${it.blockedReason}" }
            }

            if (allowedChannels.isEmpty()) {
                logger.warn { "All channels blocked for user $userId, type=$notificationType" }
            }

            // 5. Get preference snapshot
            val preferenceSnapshot = preferenceResolver.getPreferencesSnapshot(userId)

            // 6. Create notification request
            val requestId = UUID.randomUUID()
            val request = NotificationRequest(
                id = requestId,
                idempotencyKey = idempotencyKey,
                userId = userId,
                notificationType = notificationType,
                templateCode = templateCode,
                variables = variables,
                requestedChannels = resolvedChannels,
                resolvedChannels = allowedChannels,
                priority = priority,
                status = if (allowedChannels.isEmpty()) NotificationRequestStatus.COMPLETED else NotificationRequestStatus.PENDING,
                scheduledAt = scheduledAt,
                preferenceSnapshot = preferenceSnapshot,
                correlationId = correlationId ?: UUID.randomUUID().toString()
            )
            val savedRequest = requestRepository.save(request)

            StructuredLogger.notificationRequested(
                requestId, userId, notificationType.name, allowedChannels.map { it.name }
            )

            // 7. Create delivery records for each allowed channel
            allowedChannels.forEach { channel ->
                val dedupKey = deduplicationService.generateDedupKey(
                    userId, notificationType.name, channel.name, templateCode
                )

                // Check deduplication (skip for SECURITY type)
                if (notificationType != NotificationType.SECURITY && deduplicationService.isDuplicate(dedupKey)) {
                    logger.info { "Dedup hit for channel $channel, skipping delivery creation" }
                    deliveryRepository.save(
                        NotificationDelivery(
                            notificationRequestId = savedRequest.id!!,
                            channel = channel,
                            status = DeliveryStatus.SKIPPED,
                            dedupKey = dedupKey,
                            maxAttempts = retryPolicyService.getMaxAttempts()
                        )
                    )
                    return@forEach
                }

                deliveryRepository.save(
                    NotificationDelivery(
                        notificationRequestId = savedRequest.id!!,
                        channel = channel,
                        status = DeliveryStatus.PENDING,
                        dedupKey = dedupKey,
                        maxAttempts = retryPolicyService.getMaxAttempts()
                    )
                )
            }

            // 8. Create delivery records for blocked channels (as SKIPPED)
            blockedChannels.forEach { resolution ->
                deliveryRepository.save(
                    NotificationDelivery(
                        notificationRequestId = savedRequest.id!!,
                        channel = resolution.channel,
                        status = DeliveryStatus.SKIPPED,
                        errorCode = "PREFERENCE_BLOCKED",
                        errorMessage = resolution.blockedReason,
                        maxAttempts = 0
                    )
                )
            }

            // 9. Confirm idempotency
            idempotencyGuard.confirm(idempotencyKey, savedRequest.id!!)

            return savedRequest
        } catch (e: Exception) {
            idempotencyGuard.release(idempotencyKey)
            throw e
        }
    }
}
