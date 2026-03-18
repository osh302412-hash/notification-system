package com.notification.api.dto.response

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.NotificationType
import com.notification.domain.model.UserNotificationPreference
import java.time.LocalTime

data class PreferenceResponse(
    val notificationType: NotificationType,
    val channel: NotificationChannel,
    val enabled: Boolean,
    val quietHoursEnabled: Boolean,
    val quietHoursStart: LocalTime?,
    val quietHoursEnd: LocalTime?
) {
    companion object {
        fun from(pref: UserNotificationPreference): PreferenceResponse {
            return PreferenceResponse(
                notificationType = pref.notificationType,
                channel = pref.channel,
                enabled = pref.enabled,
                quietHoursEnabled = pref.quietHoursEnabled,
                quietHoursStart = pref.quietHoursStart,
                quietHoursEnd = pref.quietHoursEnd
            )
        }
    }
}
