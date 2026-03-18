package com.notification.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "notification_requests")
class NotificationRequestEntity(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "idempotency_key", nullable = false, unique = true)
    val idempotencyKey: String = "",

    @Column(name = "user_id", nullable = false)
    val userId: UUID = UUID.randomUUID(),

    @Column(name = "notification_type", nullable = false, length = 50)
    val notificationType: String = "",

    @Column(name = "template_code", nullable = false, length = 100)
    val templateCode: String = "",

    @Column(columnDefinition = "jsonb")
    val variables: String = "{}",

    @Column(name = "requested_channels", columnDefinition = "TEXT[]")
    val requestedChannels: String = "",

    @Column(name = "resolved_channels", columnDefinition = "TEXT[]")
    var resolvedChannels: String? = null,

    @Column(nullable = false, length = 20)
    val priority: String = "NORMAL",

    @Column(nullable = false, length = 30)
    var status: String = "PENDING",

    @Column(name = "scheduled_at")
    val scheduledAt: ZonedDateTime? = null,

    @Column(name = "preference_snapshot", columnDefinition = "jsonb")
    var preferenceSnapshot: String? = null,

    @Column(name = "correlation_id", length = 100)
    val correlationId: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: ZonedDateTime = ZonedDateTime.now()
)
