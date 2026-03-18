package com.notification.infrastructure.persistence.repository

import com.notification.infrastructure.persistence.entity.NotificationDeliveryAttemptEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaNotificationDeliveryAttemptRepository : JpaRepository<NotificationDeliveryAttemptEntity, UUID> {
    fun findByDeliveryId(deliveryId: UUID): List<NotificationDeliveryAttemptEntity>
}
