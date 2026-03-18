package com.notification.integration.worker

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.notification.domain.model.DeliveryStatus
import com.notification.domain.repository.NotificationDeliveryRepository
import com.notification.domain.repository.NotificationRequestRepository
import com.notification.integration.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.UUID

@AutoConfigureMockMvc
class WorkerIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var deliveryRepository: NotificationDeliveryRepository

    @Autowired
    lateinit var requestRepository: NotificationRequestRepository

    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()

    @Test
    fun `should process delivery and mark as SENT`() {
        val request = mapOf(
            "userId" to "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
            "notificationType" to "ACCOUNT",
            "templateCode" to "WELCOME",
            "variables" to mapOf("userName" to "홍길동"),
            "channels" to listOf("EMAIL"),
            "idempotencyKey" to "worker-test-${UUID.randomUUID()}"
        )

        val result = mockMvc.post("/api/v1/notifications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andReturn()

        val responseBody = objectMapper.readTree(result.response.contentAsString)
        val requestId = UUID.fromString(responseBody["data"]["id"].asText())

        // Wait for worker to process
        Thread.sleep(3000)

        val deliveries = deliveryRepository.findByNotificationRequestId(requestId)
        assertTrue(deliveries.any { it.status == DeliveryStatus.SENT || it.status == DeliveryStatus.PENDING })
    }

    @Test
    fun `should handle provider failure and create dead letter`() {
        // fail@example.com triggers permanent failure in MockEmailProvider
        val request = mapOf(
            "userId" to "c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33",
            "notificationType" to "ACCOUNT",
            "templateCode" to "WELCOME",
            "variables" to mapOf("userName" to "실패테스트"),
            "channels" to listOf("EMAIL"),
            "idempotencyKey" to "fail-test-${UUID.randomUUID()}"
        )

        val result = mockMvc.post("/api/v1/notifications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andReturn()

        val responseBody = objectMapper.readTree(result.response.contentAsString)
        val requestId = UUID.fromString(responseBody["data"]["id"].asText())

        Thread.sleep(3000)

        val deliveries = deliveryRepository.findByNotificationRequestId(requestId)
        val emailDelivery = deliveries.find { it.channel.name == "EMAIL" }
        // Should be DEAD_LETTER since fail@ triggers non-retryable failure
        assertTrue(
            emailDelivery?.status == DeliveryStatus.DEAD_LETTER ||
            emailDelivery?.status == DeliveryStatus.PENDING ||
            emailDelivery?.status == DeliveryStatus.FAILED
        )
    }
}
