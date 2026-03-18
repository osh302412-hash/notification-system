package com.notification.domain.model

import java.time.ZonedDateTime
import java.util.UUID

data class IdempotencyRecord(
    val id: UUID? = null,
    val idempotencyKey: String,
    val notificationRequestId: UUID? = null,
    val status: String,
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val expiresAt: ZonedDateTime
)
