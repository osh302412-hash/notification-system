package com.notification.domain.model

import java.time.ZonedDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val phoneNumber: String?,
    val deviceToken: String?,
    val name: String,
    val timezone: String = "Asia/Seoul",
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val updatedAt: ZonedDateTime = ZonedDateTime.now()
)
