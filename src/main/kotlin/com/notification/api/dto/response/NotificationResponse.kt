package com.notification.api.dto.response

import com.notification.domain.model.*
import java.time.ZonedDateTime
import java.util.UUID

data class NotificationResponse(
    val id: UUID,
    val idempotencyKey: String,
    val userId: UUID,
    val notificationType: NotificationType,
    val templateCode: String,
    val requestedChannels: List<NotificationChannel>,
    val resolvedChannels: List<NotificationChannel>?,
    val priority: NotificationPriority,
    val status: NotificationRequestStatus,
    val createdAt: ZonedDateTime,
    val deliveries: List<DeliveryResponse>? = null
) {
    companion object {
        fun from(request: NotificationRequest, deliveries: List<NotificationDelivery>? = null): NotificationResponse {
            return NotificationResponse(
                id = request.id!!,
                idempotencyKey = request.idempotencyKey,
                userId = request.userId,
                notificationType = request.notificationType,
                templateCode = request.templateCode,
                requestedChannels = request.requestedChannels,
                resolvedChannels = request.resolvedChannels,
                priority = request.priority,
                status = request.status,
                createdAt = request.createdAt,
                deliveries = deliveries?.map { DeliveryResponse.from(it) }
            )
        }
    }
}

data class DeliveryResponse(
    val id: UUID,
    val channel: NotificationChannel,
    val provider: String?,
    val status: DeliveryStatus,
    val attemptCount: Int,
    val errorCode: String?,
    val errorMessage: String?,
    val completedAt: ZonedDateTime?,
    val createdAt: ZonedDateTime
) {
    companion object {
        fun from(delivery: NotificationDelivery): DeliveryResponse {
            return DeliveryResponse(
                id = delivery.id!!,
                channel = delivery.channel,
                provider = delivery.provider,
                status = delivery.status,
                attemptCount = delivery.attemptCount,
                errorCode = delivery.errorCode,
                errorMessage = delivery.errorMessage,
                completedAt = delivery.completedAt,
                createdAt = delivery.createdAt
            )
        }
    }
}
