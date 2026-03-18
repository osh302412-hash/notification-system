package com.notification.api.dto.request

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.NotificationType
import java.time.LocalTime

data class UpdatePreferencesRequest(
    val preferences: List<PreferenceItem>
)

data class PreferenceItem(
    val notificationType: NotificationType,
    val channel: NotificationChannel,
    val enabled: Boolean,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: LocalTime? = null,
    val quietHoursEnd: LocalTime? = null
)
