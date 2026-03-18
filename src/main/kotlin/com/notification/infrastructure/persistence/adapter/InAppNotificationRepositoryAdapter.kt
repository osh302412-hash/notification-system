package com.notification.infrastructure.persistence.adapter

import com.notification.domain.model.InAppNotification
import com.notification.domain.repository.InAppNotificationRepository
import com.notification.infrastructure.persistence.mapper.EntityMapper
import com.notification.infrastructure.persistence.repository.JpaInAppNotificationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
class InAppNotificationRepositoryAdapter(
    private val jpaRepository: JpaInAppNotificationRepository
) : InAppNotificationRepository {
    override fun save(notification: InAppNotification): InAppNotification =
        EntityMapper.toDomain(jpaRepository.save(EntityMapper.toEntity(notification)))

    override fun findByUserId(userId: UUID, limit: Int, offset: Int): List<InAppNotification> =
        jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(offset / limit, limit))
            .map { EntityMapper.toDomain(it) }

    override fun findById(id: UUID): InAppNotification? =
        jpaRepository.findById(id).orElse(null)?.let { EntityMapper.toDomain(it) }

    @Transactional
    override fun markAsRead(id: UUID) {
        jpaRepository.markAsRead(id)
    }

    override fun countUnreadByUserId(userId: UUID): Long =
        jpaRepository.countByUserIdAndRead(userId, false)
}
