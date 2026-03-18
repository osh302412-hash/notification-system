package com.notification.domain.model

import java.time.ZonedDateTime
import java.util.UUID

data class NotificationDelivery(
    val id: UUID? = null,
    val notificationRequestId: UUID,
    val channel: NotificationChannel,
    val provider: String? = null,
    val status: DeliveryStatus = DeliveryStatus.PENDING,
    val dedupKey: String? = null,
    val attemptCount: Int = 0,
    val maxAttempts: Int = 3,
    val nextRetryAt: ZonedDateTime? = null,
    val lastAttemptedAt: ZonedDateTime? = null,
    val completedAt: ZonedDateTime? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val updatedAt: ZonedDateTime = ZonedDateTime.now()
)
