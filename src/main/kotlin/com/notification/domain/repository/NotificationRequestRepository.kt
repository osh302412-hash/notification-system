package com.notification.domain.repository

import com.notification.domain.model.NotificationRequest
import com.notification.domain.model.NotificationRequestStatus
import java.util.UUID

interface NotificationRequestRepository {
    fun save(request: NotificationRequest): NotificationRequest
    fun findById(id: UUID): NotificationRequest?
    fun findByIdempotencyKey(key: String): NotificationRequest?
    fun findByStatus(status: NotificationRequestStatus, limit: Int): List<NotificationRequest>
    fun updateStatus(id: UUID, status: NotificationRequestStatus, resolvedChannels: List<com.notification.domain.model.NotificationChannel>? = null, preferenceSnapshot: Map<String, Any>? = null)
}
