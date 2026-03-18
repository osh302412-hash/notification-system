package com.notification.domain.model

import java.time.ZonedDateTime
import java.util.UUID

data class InAppNotification(
    val id: UUID? = null,
    val userId: UUID,
    val notificationRequestId: UUID? = null,
    val title: String,
    val body: String,
    val notificationType: NotificationType,
    val data: Map<String, Any> = emptyMap(),
    val read: Boolean = false,
    val readAt: ZonedDateTime? = null,
    val createdAt: ZonedDateTime = ZonedDateTime.now()
)
