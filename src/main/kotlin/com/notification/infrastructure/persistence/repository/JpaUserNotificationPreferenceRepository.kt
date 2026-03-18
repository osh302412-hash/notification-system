package com.notification.infrastructure.persistence.repository

import com.notification.infrastructure.persistence.entity.UserNotificationPreferenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaUserNotificationPreferenceRepository : JpaRepository<UserNotificationPreferenceEntity, UUID> {
    fun findByUserId(userId: UUID): List<UserNotificationPreferenceEntity>
    fun findByUserIdAndNotificationTypeAndChannel(userId: UUID, notificationType: String, channel: String): UserNotificationPreferenceEntity?
}
