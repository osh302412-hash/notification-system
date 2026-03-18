package com.notification.infrastructure.persistence.repository

import com.notification.infrastructure.persistence.entity.NotificationTemplateEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface JpaNotificationTemplateRepository : JpaRepository<NotificationTemplateEntity, UUID> {
    fun findByCodeAndChannel(code: String, channel: String): NotificationTemplateEntity?
    fun findByCode(code: String): List<NotificationTemplateEntity>

    @Query("SELECT t FROM NotificationTemplateEntity t WHERE t.code = :code AND t.channel = :channel AND t.active = true ORDER BY t.version DESC LIMIT 1")
    fun findActiveByCodeAndChannel(code: String, channel: String): NotificationTemplateEntity?
}
