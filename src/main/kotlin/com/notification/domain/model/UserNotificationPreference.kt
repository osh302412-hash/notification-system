package com.notification.domain.model

import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.UUID

data class UserNotificationPreference(
    val id: UUID? = null,
    val userId: UUID,
    val notificationType: NotificationType,
    val channel: NotificationChannel,
    val enabled: Boolean = true,
    val quietHoursStart: LocalTime? = null,
    val quietHoursEnd: LocalTime? = null,
    val quietHoursEnabled: Boolean = false,
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val updatedAt: ZonedDateTime = ZonedDateTime.now()
)
