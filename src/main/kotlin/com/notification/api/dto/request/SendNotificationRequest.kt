package com.notification.api.dto.request

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.NotificationPriority
import com.notification.domain.model.NotificationType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.ZonedDateTime
import java.util.UUID

data class SendNotificationRequest(
    @field:NotNull(message = "userId is required")
    val userId: UUID,

    @field:NotNull(message = "notificationType is required")
    val notificationType: NotificationType,

    @field:NotBlank(message = "templateCode is required")
    val templateCode: String,

    val variables: Map<String, String> = emptyMap(),

    val channels: List<NotificationChannel>? = null,

    val priority: NotificationPriority = NotificationPriority.NORMAL,

    @field:NotBlank(message = "idempotencyKey is required")
    val idempotencyKey: String,

    val scheduledAt: ZonedDateTime? = null
)
