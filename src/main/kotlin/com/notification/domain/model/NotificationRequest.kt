package com.notification.domain.model

import java.time.ZonedDateTime
import java.util.UUID

data class NotificationRequest(
    val id: UUID? = null,
    val idempotencyKey: String,
    val userId: UUID,
    val notificationType: NotificationType,
    val templateCode: String,
    val variables: Map<String, String> = emptyMap(),
    val requestedChannels: List<NotificationChannel>,
    val resolvedChannels: List<NotificationChannel>? = null,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val status: NotificationRequestStatus = NotificationRequestStatus.PENDING,
    val scheduledAt: ZonedDateTime? = null,
    val preferenceSnapshot: Map<String, Any>? = null,
    val correlationId: String? = null,
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val updatedAt: ZonedDateTime = ZonedDateTime.now()
)
