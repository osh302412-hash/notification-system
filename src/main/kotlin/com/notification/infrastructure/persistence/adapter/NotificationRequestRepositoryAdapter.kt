package com.notification.infrastructure.persistence.adapter

import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.NotificationRequest
import com.notification.domain.model.NotificationRequestStatus
import com.notification.domain.repository.NotificationRequestRepository
import com.notification.infrastructure.persistence.mapper.EntityMapper
import com.notification.infrastructure.persistence.repository.JpaNotificationRequestRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
class NotificationRequestRepositoryAdapter(
    private val jpaRepository: JpaNotificationRequestRepository
) : NotificationRequestRepository {
    override fun save(request: NotificationRequest): NotificationRequest =
        EntityMapper.toDomain(jpaRepository.save(EntityMapper.toEntity(request)))

    override fun findById(id: UUID): NotificationRequest? =
        jpaRepository.findById(id).orElse(null)?.let { EntityMapper.toDomain(it) }

    override fun findByIdempotencyKey(key: String): NotificationRequest? =
        jpaRepository.findByIdempotencyKey(key)?.let { EntityMapper.toDomain(it) }

    override fun findByStatus(status: NotificationRequestStatus, limit: Int): List<NotificationRequest> =
        jpaRepository.findByStatusOrderByCreatedAt(status.name, limit).map { EntityMapper.toDomain(it) }

    @Transactional
    override fun updateStatus(id: UUID, status: NotificationRequestStatus, resolvedChannels: List<NotificationChannel>?, preferenceSnapshot: Map<String, Any>?) {
        val entity = jpaRepository.findById(id).orElseThrow()
        entity.status = status.name
        resolvedChannels?.let { entity.resolvedChannels = it.map { ch -> ch.name }.toTypedArray() }
        preferenceSnapshot?.let {
            val objectMapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
            entity.preferenceSnapshot = objectMapper.writeValueAsString(it)
        }
        entity.updatedAt = java.time.ZonedDateTime.now()
        jpaRepository.save(entity)
    }
}
