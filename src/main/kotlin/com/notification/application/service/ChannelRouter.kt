package com.notification.application.service

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.NotificationType
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ChannelRouter {

    // Default channel policies per notification type
    private val defaultChannelPolicies: Map<NotificationType, List<NotificationChannel>> = mapOf(
        NotificationType.ACCOUNT to listOf(NotificationChannel.EMAIL, NotificationChannel.IN_APP),
        NotificationType.TRANSACTION to listOf(NotificationChannel.SMS, NotificationChannel.PUSH, NotificationChannel.IN_APP),
        NotificationType.SECURITY to listOf(NotificationChannel.EMAIL, NotificationChannel.SMS, NotificationChannel.PUSH, NotificationChannel.IN_APP),
        NotificationType.MARKETING to listOf(NotificationChannel.EMAIL, NotificationChannel.PUSH, NotificationChannel.IN_APP)
    )

    fun resolveChannels(
        notificationType: NotificationType,
        requestedChannels: List<NotificationChannel>?
    ): List<NotificationChannel> {
        // If channels explicitly requested, use them
        if (!requestedChannels.isNullOrEmpty()) {
            logger.debug { "Using explicitly requested channels: $requestedChannels" }
            return requestedChannels
        }

        // Otherwise use default policy
        val defaults = defaultChannelPolicies[notificationType]
            ?: listOf(NotificationChannel.IN_APP)

        logger.debug { "Using default channels for $notificationType: $defaults" }
        return defaults
    }
}
