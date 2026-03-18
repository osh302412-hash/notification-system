package com.notification.domain.repository

import com.notification.domain.model.NotificationDeliveryAttempt
import java.util.UUID

interface NotificationDeliveryAttemptRepository {
    fun save(attempt: NotificationDeliveryAttempt): NotificationDeliveryAttempt
    fun findByDeliveryId(deliveryId: UUID): List<NotificationDeliveryAttempt>
}
