package com.notification.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "notification_templates")
class NotificationTemplateEntity(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 100)
    val code: String = "",

    @Column(nullable = false)
    val version: Int = 1,

    @Column(name = "notification_type", nullable = false, length = 50)
    val notificationType: String = "",

    @Column(nullable = false, length = 20)
    val channel: String = "",

    @Column(name = "title_template", nullable = false, columnDefinition = "TEXT")
    val titleTemplate: String = "",

    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    val bodyTemplate: String = "",

    @Column(name = "default_variables", columnDefinition = "jsonb")
    val defaultVariables: String = "{}",

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: ZonedDateTime = ZonedDateTime.now()
)
