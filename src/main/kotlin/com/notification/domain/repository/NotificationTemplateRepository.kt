package com.notification.domain.repository

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.NotificationTemplate

interface NotificationTemplateRepository {
    fun findByCodeAndChannel(code: String, channel: NotificationChannel): NotificationTemplate?
    fun findByCode(code: String): List<NotificationTemplate>
    fun findActiveByCodeAndChannel(code: String, channel: NotificationChannel): NotificationTemplate?
}
