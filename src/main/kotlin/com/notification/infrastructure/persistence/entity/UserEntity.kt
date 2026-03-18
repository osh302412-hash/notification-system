package com.notification.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    val email: String = "",

    @Column(name = "phone_number")
    val phoneNumber: String? = null,

    @Column(name = "device_token")
    val deviceToken: String? = null,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = false)
    val timezone: String = "Asia/Seoul",

    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: ZonedDateTime = ZonedDateTime.now()
)
