package com.notification.infrastructure.persistence.adapter

import com.notification.domain.model.DeliveryStatus
import com.notification.domain.model.NotificationDelivery
import com.notification.domain.repository.NotificationDeliveryRepository
import com.notification.infrastructure.persistence.mapper.EntityMapper
import com.notification.infrastructure.persistence.repository.JpaNotificationDeliveryRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.UUID

@Repository
class NotificationDeliveryRepositoryAdapter(
    private val jpaRepository: JpaNotificationDeliveryRepository
) : NotificationDeliveryRepository {
    override fun save(delivery: NotificationDelivery): NotificationDelivery =
        EntityMapper.toDomain(jpaRepository.save(EntityMapper.toEntity(delivery)))

    override fun findById(id: UUID): NotificationDelivery? =
        jpaRepository.findById(id).orElse(null)?.let { EntityMapper.toDomain(it) }

    override fun findByNotificationRequestId(requestId: UUID): List<NotificationDelivery> =
        jpaRepository.findByNotificationRequestId(requestId).map { EntityMapper.toDomain(it) }

    override fun findByStatusAndNextRetryBefore(status: DeliveryStatus, before: ZonedDateTime, limit: Int): List<NotificationDelivery> =
        jpaRepository.findByStatusAndNextRetryBefore(status.name, before, limit).map { EntityMapper.toDomain(it) }

    override fun findPendingDeliveries(limit: Int): List<NotificationDelivery> =
        jpaRepository.findPendingDeliveries(limit).map { EntityMapper.toDomain(it) }

    @Transactional
    override fun updateStatus(id: UUID, status: DeliveryStatus, errorCode: String?, errorMessage: String?, provider: String?) {
        val entity = jpaRepository.findById(id).orElseThrow()
        entity.status = status.name
        entity.errorCode = errorCode
        entity.errorMessage = errorMessage
        provider?.let { entity.provider = it }
        entity.updatedAt = ZonedDateTime.now()
        jpaRepository.save(entity)
    }

    @Transactional
    override fun incrementAttemptAndScheduleRetry(id: UUID, nextRetryAt: ZonedDateTime?) {
        val entity = jpaRepository.findById(id).orElseThrow()
        entity.attemptCount += 1
        entity.lastAttemptedAt = ZonedDateTime.now()
        entity.nextRetryAt = nextRetryAt
        entity.status = if (nextRetryAt != null) DeliveryStatus.RETRY_PENDING.name else entity.status
        entity.updatedAt = ZonedDateTime.now()
        jpaRepository.save(entity)
    }

    @Transactional
    override fun markCompleted(id: UUID, provider: String) {
        val entity = jpaRepository.findById(id).orElseThrow()
        entity.status = DeliveryStatus.SENT.name
        entity.provider = provider
        entity.completedAt = ZonedDateTime.now()
        entity.updatedAt = ZonedDateTime.now()
        jpaRepository.save(entity)
    }

    @Transactional
    override fun markDeadLetter(id: UUID, errorCode: String?, errorMessage: String?) {
        val entity = jpaRepository.findById(id).orElseThrow()
        entity.status = DeliveryStatus.DEAD_LETTER.name
        entity.errorCode = errorCode
        entity.errorMessage = errorMessage
        entity.updatedAt = ZonedDateTime.now()
        jpaRepository.save(entity)
    }
}
