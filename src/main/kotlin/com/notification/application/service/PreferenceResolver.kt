package com.notification.application.service

import com.notification.common.logging.StructuredLogger
import com.notification.domain.model.*
import com.notification.domain.repository.UserNotificationPreferenceRepository
import com.notification.infrastructure.config.NotificationProperties
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

data class ChannelResolution(
    val channel: NotificationChannel,
    val allowed: Boolean,
    val blockedReason: String? = null
)

@Service
class PreferenceResolver(
    private val preferenceRepository: UserNotificationPreferenceRepository,
    private val properties: NotificationProperties
) {
    fun resolveChannels(
        userId: UUID,
        notificationType: NotificationType,
        requestedChannels: List<NotificationChannel>,
        priority: NotificationPriority,
        timezone: String = "Asia/Seoul"
    ): List<ChannelResolution> {
        return requestedChannels.map { channel ->
            resolveChannel(userId, notificationType, channel, priority, timezone)
        }
    }

    fun resolveChannel(
        userId: UUID,
        notificationType: NotificationType,
        channel: NotificationChannel,
        priority: NotificationPriority,
        timezone: String = "Asia/Seoul"
    ): ChannelResolution {
        val preference = preferenceRepository.findByUserIdAndNotificationTypeAndChannel(
            userId, notificationType, channel
        )

        // No preference found - check notification type
        if (preference == null) {
            // MARKETING requires explicit opt-in
            if (notificationType == NotificationType.MARKETING) {
                StructuredLogger.preferenceBlocked(userId, channel.name, "No marketing consent")
                return ChannelResolution(channel, false, "Marketing requires explicit consent")
            }
            // Other types default to allowed
            return ChannelResolution(channel, true)
        }

        // Check enabled
        if (!preference.enabled) {
            StructuredLogger.preferenceBlocked(userId, channel.name, "Channel disabled")
            return ChannelResolution(channel, false, "Channel disabled by user")
        }

        // Check quiet hours (HIGH/URGENT priority bypasses)
        if (priority != NotificationPriority.HIGH && priority != NotificationPriority.URGENT) {
            if (isInQuietHours(preference, timezone)) {
                StructuredLogger.quietHoursBlocked(userId, channel.name)
                return ChannelResolution(channel, false, "Quiet hours active")
            }
        }

        return ChannelResolution(channel, true)
    }

    fun isInQuietHours(preference: UserNotificationPreference, timezone: String): Boolean {
        if (!preference.quietHoursEnabled) return false

        val start = preference.quietHoursStart ?: LocalTime.parse(properties.quietHours.defaultStart)
        val end = preference.quietHoursEnd ?: LocalTime.parse(properties.quietHours.defaultEnd)
        val now = ZonedDateTime.now(ZoneId.of(timezone)).toLocalTime()

        return if (start.isAfter(end)) {
            // Overnight: e.g., 22:00 ~ 08:00
            now.isAfter(start) || now.isBefore(end)
        } else {
            now.isAfter(start) && now.isBefore(end)
        }
    }

    fun getPreferencesSnapshot(userId: UUID): Map<String, Any> {
        val preferences = preferenceRepository.findByUserId(userId)
        return mapOf(
            "userId" to userId.toString(),
            "preferences" to preferences.map {
                mapOf(
                    "type" to it.notificationType.name,
                    "channel" to it.channel.name,
                    "enabled" to it.enabled,
                    "quietHoursEnabled" to it.quietHoursEnabled
                )
            }
        )
    }
}
