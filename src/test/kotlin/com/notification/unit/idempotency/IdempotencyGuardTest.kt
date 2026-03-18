package com.notification.unit.idempotency

import com.notification.application.service.IdempotencyGuard
import com.notification.domain.model.IdempotencyRecord
import com.notification.domain.repository.IdempotencyRepository
import com.notification.infrastructure.config.IdempotencyProperties
import com.notification.infrastructure.config.NotificationProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

class IdempotencyGuardTest {
    private val idempotencyRepository = mockk<IdempotencyRepository>(relaxed = true)
    private val redisTemplate = mockk<StringRedisTemplate>()
    private val valueOps = mockk<ValueOperations<String, String>>()
    private val properties = NotificationProperties().apply {
        idempotency = IdempotencyProperties().apply { ttlHours = 24 }
    }

    init {
        every { redisTemplate.opsForValue() } returns valueOps
    }

    private val guard = IdempotencyGuard(idempotencyRepository, redisTemplate, properties)

    @Test
    fun `should return null for new request`() {
        every { valueOps.get("idempotency:new-key") } returns null
        every { idempotencyRepository.findByKey("new-key") } returns null
        every { valueOps.setIfAbsent("idempotency:new-key", "PENDING", 24, TimeUnit.HOURS) } returns true

        val result = guard.checkAndAcquire("new-key")

        assertNull(result)
    }

    @Test
    fun `should return existing request ID from Redis cache`() {
        val existingId = UUID.randomUUID()
        every { valueOps.get("idempotency:existing-key") } returns existingId.toString()

        val result = guard.checkAndAcquire("existing-key")

        assertEquals(existingId, result)
    }

    @Test
    fun `should return existing request ID from DB when not in Redis`() {
        val existingId = UUID.randomUUID()
        every { valueOps.get("idempotency:db-key") } returns null
        every { idempotencyRepository.findByKey("db-key") } returns IdempotencyRecord(
            idempotencyKey = "db-key",
            notificationRequestId = existingId,
            status = "CONFIRMED",
            expiresAt = ZonedDateTime.now().plusHours(24)
        )
        every { valueOps.set("idempotency:db-key", existingId.toString(), 24, TimeUnit.HOURS) } returns Unit

        val result = guard.checkAndAcquire("db-key")

        assertEquals(existingId, result)
    }

    @Test
    fun `should confirm idempotency by saving to Redis and DB`() {
        val requestId = UUID.randomUUID()
        every { valueOps.set(any(), any(), any(), any<TimeUnit>()) } returns Unit
        every { idempotencyRepository.save(any()) } returns mockk()

        guard.confirm("key", requestId)

        verify { valueOps.set("idempotency:key", requestId.toString(), 24, TimeUnit.HOURS) }
        verify { idempotencyRepository.save(any()) }
    }

    @Test
    fun `should release lock by deleting Redis key`() {
        every { redisTemplate.delete("idempotency:release-key") } returns true

        guard.release("release-key")

        verify { redisTemplate.delete("idempotency:release-key") }
    }
}
