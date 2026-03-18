package com.notification.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "idempotency_records")
class IdempotencyRecordEntity(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "idempotency_key", nullable = false, unique = true)
    val idempotencyKey: String = "",

    @Column(name = "notification_request_id")
    val notificationRequestId: UUID? = null,

    @Column(nullable = false, length = 30)
    var status: String = "",

    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "expires_at", nullable = false)
    val expiresAt: ZonedDateTime = ZonedDateTime.now()
)
