package com.notification.infrastructure.persistence.adapter

import com.notification.domain.model.NotificationDeliveryAttempt
import com.notification.domain.repository.NotificationDeliveryAttemptRepository
import com.notification.infrastructure.persistence.mapper.EntityMapper
import com.notification.infrastructure.persistence.repository.JpaNotificationDeliveryAttemptRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class NotificationDeliveryAttemptRepositoryAdapter(
    private val jpaRepository: JpaNotificationDeliveryAttemptRepository
) : NotificationDeliveryAttemptRepository {
    override fun save(attempt: NotificationDeliveryAttempt): NotificationDeliveryAttempt =
        EntityMapper.toDomain(jpaRepository.save(EntityMapper.toEntity(attempt)))

    override fun findByDeliveryId(deliveryId: UUID): List<NotificationDeliveryAttempt> =
        jpaRepository.findByDeliveryId(deliveryId).map { EntityMapper.toDomain(it) }
}
