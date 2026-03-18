package com.notification.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "in_app_notifications")
class InAppNotificationEntity(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID = UUID.randomUUID(),

    @Column(name = "notification_request_id")
    val notificationRequestId: UUID? = null,

    @Column(nullable = false, length = 500)
    val title: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val body: String = "",

    @Column(name = "notification_type", nullable = false, length = 50)
    val notificationType: String = "",

    @Column(columnDefinition = "jsonb")
    val data: String = "{}",

    @Column(name = "read", nullable = false)
    var read: Boolean = false,

    @Column(name = "read_at")
    var readAt: ZonedDateTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now()
)
