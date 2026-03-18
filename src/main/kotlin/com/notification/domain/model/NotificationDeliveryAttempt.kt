package com.notification.domain.model

import java.time.ZonedDateTime
import java.util.UUID

data class NotificationDeliveryAttempt(
    val id: UUID? = null,
    val deliveryId: UUID,
    val attemptNumber: Int,
    val provider: String,
    val status: DeliveryAttemptStatus,
    val requestPayload: Map<String, Any>? = null,
    val responsePayload: Map<String, Any>? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val durationMs: Long? = null,
    val createdAt: ZonedDateTime = ZonedDateTime.now()
)
