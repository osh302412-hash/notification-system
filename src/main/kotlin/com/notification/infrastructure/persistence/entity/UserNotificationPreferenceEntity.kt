package com.notification.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "user_notification_preferences")
class UserNotificationPreferenceEntity(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID = UUID.randomUUID(),

    @Column(name = "notification_type", nullable = false, length = 50)
    val notificationType: String = "",

    @Column(nullable = false, length = 20)
    val channel: String = "",

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(name = "quiet_hours_start")
    var quietHoursStart: LocalTime? = null,

    @Column(name = "quiet_hours_end")
    var quietHoursEnd: LocalTime? = null,

    @Column(name = "quiet_hours_enabled", nullable = false)
    var quietHoursEnabled: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: ZonedDateTime = ZonedDateTime.now()
)
