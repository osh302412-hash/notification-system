package com.notification.infrastructure.persistence.adapter

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.NotificationType
import com.notification.domain.model.UserNotificationPreference
import com.notification.domain.repository.UserNotificationPreferenceRepository
import com.notification.infrastructure.persistence.mapper.EntityMapper
import com.notification.infrastructure.persistence.repository.JpaUserNotificationPreferenceRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserNotificationPreferenceRepositoryAdapter(
    private val jpaRepository: JpaUserNotificationPreferenceRepository
) : UserNotificationPreferenceRepository {
    override fun findByUserId(userId: UUID): List<UserNotificationPreference> =
        jpaRepository.findByUserId(userId).map { EntityMapper.toDomain(it) }

    override fun findByUserIdAndNotificationTypeAndChannel(
        userId: UUID,
        notificationType: NotificationType,
        channel: NotificationChannel
    ): UserNotificationPreference? =
        jpaRepository.findByUserIdAndNotificationTypeAndChannel(userId, notificationType.name, channel.name)
            ?.let { EntityMapper.toDomain(it) }

    override fun saveAll(preferences: List<UserNotificationPreference>): List<UserNotificationPreference> =
        jpaRepository.saveAll(preferences.map { EntityMapper.toEntity(it) }).map { EntityMapper.toDomain(it) }

    override fun save(preference: UserNotificationPreference): UserNotificationPreference =
        EntityMapper.toDomain(jpaRepository.save(EntityMapper.toEntity(preference)))
}
