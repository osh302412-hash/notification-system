package com.notification.infrastructure.persistence.repository

import com.notification.infrastructure.persistence.entity.NotificationRequestEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface JpaNotificationRequestRepository : JpaRepository<NotificationRequestEntity, UUID> {
    fun findByIdempotencyKey(idempotencyKey: String): NotificationRequestEntity?

    @Query("SELECT r FROM NotificationRequestEntity r WHERE r.status = :status ORDER BY r.createdAt ASC LIMIT :limit")
    fun findByStatusOrderByCreatedAt(status: String, limit: Int): List<NotificationRequestEntity>

    @Modifying
    @Query("UPDATE NotificationRequestEntity r SET r.status = :status, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
    fun updateStatus(id: UUID, status: String)
}
