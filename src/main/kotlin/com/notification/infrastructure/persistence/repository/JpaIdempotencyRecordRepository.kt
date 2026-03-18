package com.notification.infrastructure.persistence.repository

import com.notification.infrastructure.persistence.entity.IdempotencyRecordEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaIdempotencyRecordRepository : JpaRepository<IdempotencyRecordEntity, UUID> {
    fun findByIdempotencyKey(idempotencyKey: String): IdempotencyRecordEntity?
    fun existsByIdempotencyKey(idempotencyKey: String): Boolean
}
