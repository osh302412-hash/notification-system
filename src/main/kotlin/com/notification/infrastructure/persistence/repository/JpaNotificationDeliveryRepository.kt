package com.notification.infrastructure.persistence.repository

import com.notification.infrastructure.persistence.entity.NotificationDeliveryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime
import java.util.UUID

interface JpaNotificationDeliveryRepository : JpaRepository<NotificationDeliveryEntity, UUID> {
    fun findByNotificationRequestId(notificationRequestId: UUID): List<NotificationDeliveryEntity>

    @Query("SELECT d FROM NotificationDeliveryEntity d WHERE d.status = :status AND d.nextRetryAt <= :before ORDER BY d.nextRetryAt ASC LIMIT :limit")
    fun findByStatusAndNextRetryBefore(status: String, before: ZonedDateTime, limit: Int): List<NotificationDeliveryEntity>

    @Query("SELECT d FROM NotificationDeliveryEntity d WHERE d.status = 'PENDING' ORDER BY d.createdAt ASC LIMIT :limit")
    fun findPendingDeliveries(limit: Int): List<NotificationDeliveryEntity>

    @Modifying
    @Query("UPDATE NotificationDeliveryEntity d SET d.status = :status, d.errorCode = :errorCode, d.errorMessage = :errorMessage, d.provider = :provider, d.updatedAt = CURRENT_TIMESTAMP WHERE d.id = :id")
    fun updateStatus(id: UUID, status: String, errorCode: String?, errorMessage: String?, provider: String?)
}
