package com.notification.infrastructure.provider

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.ProviderResult
import com.notification.domain.model.User

interface NotificationProvider {
    val channel: NotificationChannel
    val providerName: String
    fun send(user: User, title: String, body: String, metadata: Map<String, Any> = emptyMap()): ProviderResult
}
