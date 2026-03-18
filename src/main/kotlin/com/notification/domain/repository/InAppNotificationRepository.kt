package com.notification.domain.repository

import com.notification.domain.model.InAppNotification
import java.util.UUID

interface InAppNotificationRepository {
    fun save(notification: InAppNotification): InAppNotification
    fun findByUserId(userId: UUID, limit: Int = 50, offset: Int = 0): List<InAppNotification>
    fun findById(id: UUID): InAppNotification?
    fun markAsRead(id: UUID)
    fun countUnreadByUserId(userId: UUID): Long
}
