package com.notification.domain.repository

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.NotificationType
import com.notification.domain.model.UserNotificationPreference
import java.util.UUID

interface UserNotificationPreferenceRepository {
    fun findByUserId(userId: UUID): List<UserNotificationPreference>
    fun findByUserIdAndNotificationTypeAndChannel(
        userId: UUID,
        notificationType: NotificationType,
        channel: NotificationChannel
    ): UserNotificationPreference?
    fun saveAll(preferences: List<UserNotificationPreference>): List<UserNotificationPreference>
    fun save(preference: UserNotificationPreference): UserNotificationPreference
}
