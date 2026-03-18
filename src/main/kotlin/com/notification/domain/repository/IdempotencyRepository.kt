package com.notification.domain.repository

import com.notification.domain.model.IdempotencyRecord

interface IdempotencyRepository {
    fun findByKey(key: String): IdempotencyRecord?
    fun save(record: IdempotencyRecord): IdempotencyRecord
    fun existsByKey(key: String): Boolean
}
