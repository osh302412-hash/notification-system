package com.notification.domain.model

import java.time.ZonedDateTime
import java.util.UUID

data class NotificationTemplate(
    val id: UUID? = null,
    val code: String,
    val version: Int = 1,
    val notificationType: NotificationType,
    val channel: NotificationChannel,
    val titleTemplate: String,
    val bodyTemplate: String,
    val defaultVariables: Map<String, String> = emptyMap(),
    val active: Boolean = true,
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val updatedAt: ZonedDateTime = ZonedDateTime.now()
)
