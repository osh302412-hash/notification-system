package com.notification.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "notification_deliveries")
class NotificationDeliveryEntity(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "notification_request_id", nullable = false)
    val notificationRequestId: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 20)
    val channel: String = "",

    @Column(length = 50)
    var provider: String? = null,

    @Column(nullable = false, length = 30)
    var status: String = "PENDING",

    @Column(name = "dedup_key")
    val dedupKey: String? = null,

    @Column(name = "attempt_count", nullable = false)
    var attemptCount: Int = 0,

    @Column(name = "max_attempts", nullable = false)
    val maxAttempts: Int = 3,

    @Column(name = "next_retry_at")
    var nextRetryAt: ZonedDateTime? = null,

    @Column(name = "last_attempted_at")
    var lastAttemptedAt: ZonedDateTime? = null,

    @Column(name = "completed_at")
    var completedAt: ZonedDateTime? = null,

    @Column(name = "error_code", length = 100)
    var errorCode: String? = null,

    @Column(name = "error_message", columnDefinition = "TEXT")
    var errorMessage: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: ZonedDateTime = ZonedDateTime.now()
)
