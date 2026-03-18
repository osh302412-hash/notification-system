package com.notification.infrastructure.persistence.repository

import com.notification.infrastructure.persistence.entity.InAppNotificationEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface JpaInAppNotificationRepository : JpaRepository<InAppNotificationEntity, UUID> {
    @Query("SELECT n FROM InAppNotificationEntity n WHERE n.userId = :userId ORDER BY n.createdAt DESC")
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): List<InAppNotificationEntity>

    @Modifying
    @Query("UPDATE InAppNotificationEntity n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id")
    fun markAsRead(id: UUID)

    fun countByUserIdAndRead(userId: UUID, read: Boolean): Long
}
