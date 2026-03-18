package com.notification.infrastructure.persistence.adapter

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.NotificationTemplate
import com.notification.domain.repository.NotificationTemplateRepository
import com.notification.infrastructure.persistence.mapper.EntityMapper
import com.notification.infrastructure.persistence.repository.JpaNotificationTemplateRepository
import org.springframework.stereotype.Repository

@Repository
class NotificationTemplateRepositoryAdapter(
    private val jpaRepository: JpaNotificationTemplateRepository
) : NotificationTemplateRepository {
    override fun findByCodeAndChannel(code: String, channel: NotificationChannel): NotificationTemplate? =
        jpaRepository.findByCodeAndChannel(code, channel.name)?.let { EntityMapper.toDomain(it) }

    override fun findByCode(code: String): List<NotificationTemplate> =
        jpaRepository.findByCode(code).map { EntityMapper.toDomain(it) }

    override fun findActiveByCodeAndChannel(code: String, channel: NotificationChannel): NotificationTemplate? =
        jpaRepository.findActiveByCodeAndChannel(code, channel.name)?.let { EntityMapper.toDomain(it) }
}
