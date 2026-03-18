package com.notification.infrastructure.provider

import com.notification.common.exception.InvalidChannelException
import com.notification.domain.model.NotificationChannel
import org.springframework.stereotype.Component

@Component
class ProviderRegistry(
    providers: List<NotificationProvider>
) {
    private val providerMap: Map<NotificationChannel, NotificationProvider> =
        providers.associateBy { it.channel }

    fun getProvider(channel: NotificationChannel): NotificationProvider =
        providerMap[channel] ?: throw InvalidChannelException(channel.name)

    fun hasProvider(channel: NotificationChannel): Boolean =
        providerMap.containsKey(channel)
}
