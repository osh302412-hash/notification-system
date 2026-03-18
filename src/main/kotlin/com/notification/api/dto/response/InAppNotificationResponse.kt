package com.notification.api.dto.response

import com.notification.domain.model.InAppNotification
import com.notification.domain.model.NotificationType
import java.time.ZonedDateTime
import java.util.UUID

data class InAppNotificationResponse(
    val id: UUID,
    val title: String,
    val body: String,
    val notificationType: NotificationType,
    val data: Map<String, Any>,
    val read: Boolean,
    val readAt: ZonedDateTime?,
    val createdAt: ZonedDateTime
) {
    companion object {
        fun from(notification: InAppNotification): InAppNotificationResponse {
            return InAppNotificationResponse(
                id = notification.id!!,
                title = notification.title,
                body = notification.body,
                notificationType = notification.notificationType,
                data = notification.data,
                read = notification.read,
                readAt = notification.readAt,
                createdAt = notification.createdAt
            )
        }
    }
}

data class InAppNotificationListResponse(
    val notifications: List<InAppNotificationResponse>,
    val unreadCount: Long
)
