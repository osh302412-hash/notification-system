package com.notification.infrastructure.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "notification_delivery_attempts")
class NotificationDeliveryAttemptEntity(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "delivery_id", nullable = false)
    val deliveryId: UUID = UUID.randomUUID(),

    @Column(name = "attempt_number", nullable = false)
    val attemptNumber: Int = 0,

    @Column(nullable = false, length = 50)
    val provider: String = "",

    @Column(nullable = false, length = 30)
    val status: String = "",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_payload", columnDefinition = "jsonb")
    val requestPayload: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload", columnDefinition = "jsonb")
    val responsePayload: String? = null,

    @Column(name = "error_code", length = 100)
    val errorCode: String? = null,

    @Column(name = "error_message", columnDefinition = "TEXT")
    val errorMessage: String? = null,

    @Column(name = "duration_ms")
    val durationMs: Long? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now()
)
