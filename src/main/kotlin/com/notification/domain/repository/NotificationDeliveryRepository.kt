package com.notification.domain.repository

import com.notification.domain.model.DeliveryStatus
import com.notification.domain.model.NotificationDelivery
import java.time.ZonedDateTime
import java.util.UUID

interface NotificationDeliveryRepository {
    fun save(delivery: NotificationDelivery): NotificationDelivery
    fun findById(id: UUID): NotificationDelivery?
    fun findByNotificationRequestId(requestId: UUID): List<NotificationDelivery>
    fun findByStatusAndNextRetryBefore(status: DeliveryStatus, before: ZonedDateTime, limit: Int): List<NotificationDelivery>
    fun findPendingDeliveries(limit: Int): List<NotificationDelivery>
    fun updateStatus(id: UUID, status: DeliveryStatus, errorCode: String? = null, errorMessage: String? = null, provider: String? = null)
    fun incrementAttemptAndScheduleRetry(id: UUID, nextRetryAt: ZonedDateTime?)
    fun markCompleted(id: UUID, provider: String)
    fun markDeadLetter(id: UUID, errorCode: String?, errorMessage: String?)
}
