package com.notification.infrastructure.persistence.adapter

import com.notification.domain.model.IdempotencyRecord
import com.notification.domain.repository.IdempotencyRepository
import com.notification.infrastructure.persistence.mapper.EntityMapper
import com.notification.infrastructure.persistence.repository.JpaIdempotencyRecordRepository
import org.springframework.stereotype.Repository

@Repository
class IdempotencyRepositoryAdapter(
    private val jpaRepository: JpaIdempotencyRecordRepository
) : IdempotencyRepository {
    override fun findByKey(key: String): IdempotencyRecord? =
        jpaRepository.findByIdempotencyKey(key)?.let { EntityMapper.toDomain(it) }

    override fun save(record: IdempotencyRecord): IdempotencyRecord =
        EntityMapper.toDomain(jpaRepository.save(EntityMapper.toEntity(record)))

    override fun existsByKey(key: String): Boolean =
        jpaRepository.existsByIdempotencyKey(key)
}
