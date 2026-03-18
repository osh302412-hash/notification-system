package com.notification.api.dto.response

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.NotificationType
import com.notification.domain.model.NotificationTemplate

data class TemplateResponse(
    val code: String,
    val version: Int,
    val notificationType: NotificationType,
    val channel: NotificationChannel,
    val titleTemplate: String,
    val bodyTemplate: String,
    val active: Boolean
) {
    companion object {
        fun from(template: NotificationTemplate): TemplateResponse {
            return TemplateResponse(
                code = template.code,
                version = template.version,
                notificationType = template.notificationType,
                channel = template.channel,
                titleTemplate = template.titleTemplate,
                bodyTemplate = template.bodyTemplate,
                active = template.active
            )
        }
    }
}

data class RenderedTemplateResponse(
    val title: String,
    val body: String,
    val templateVersion: Int
)
