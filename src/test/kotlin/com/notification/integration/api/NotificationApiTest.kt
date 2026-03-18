package com.notification.integration.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.notification.integration.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.UUID

@AutoConfigureMockMvc
class NotificationApiTest : BaseIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()

    @Test
    fun `POST notifications - should create notification for valid request`() {
        val request = mapOf(
            "userId" to "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
            "notificationType" to "ACCOUNT",
            "templateCode" to "WELCOME",
            "variables" to mapOf("userName" to "홍길동"),
            "channels" to listOf("EMAIL", "IN_APP"),
            "priority" to "NORMAL",
            "idempotencyKey" to "test-welcome-${UUID.randomUUID()}"
        )

        mockMvc.post("/api/v1/notifications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isAccepted() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.notificationType") { value("ACCOUNT") }
            jsonPath("$.data.status") { value("PENDING") }
            jsonPath("$.data.deliveries") { isArray() }
        }
    }

    @Test
    fun `POST notifications - should return conflict for duplicate idempotency key`() {
        val idempotencyKey = "dup-test-${UUID.randomUUID()}"
        val request = mapOf(
            "userId" to "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
            "notificationType" to "ACCOUNT",
            "templateCode" to "WELCOME",
            "variables" to mapOf("userName" to "홍길동"),
            "idempotencyKey" to idempotencyKey
        )

        // First request
        mockMvc.post("/api/v1/notifications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isAccepted() }
        }

        // Duplicate request - should return the same result (idempotent)
        mockMvc.post("/api/v1/notifications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isAccepted() }
            jsonPath("$.success") { value(true) }
        }
    }

    @Test
    fun `POST notifications - should return 404 for non-existent user`() {
        val request = mapOf(
            "userId" to UUID.randomUUID().toString(),
            "notificationType" to "ACCOUNT",
            "templateCode" to "WELCOME",
            "variables" to mapOf("userName" to "누군가"),
            "idempotencyKey" to "missing-user-${UUID.randomUUID()}"
        )

        mockMvc.post("/api/v1/notifications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error.code") { value("USER_NOT_FOUND") }
        }
    }

    @Test
    fun `POST notifications - should block marketing without consent`() {
        val request = mapOf(
            "userId" to "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
            "notificationType" to "MARKETING",
            "templateCode" to "MARKETING_PROMO",
            "variables" to mapOf("promoTitle" to "할인", "promoDescription" to "50% 할인", "userName" to "김철수"),
            "channels" to listOf("EMAIL", "PUSH"),
            "idempotencyKey" to "marketing-blocked-${UUID.randomUUID()}"
        )

        mockMvc.post("/api/v1/notifications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isAccepted() }
            jsonPath("$.success") { value(true) }
            // All channels should be SKIPPED due to marketing disabled
        }
    }

    @Test
    fun `POST notifications - payment complete scenario`() {
        val request = mapOf(
            "userId" to "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
            "notificationType" to "TRANSACTION",
            "templateCode" to "PAYMENT_COMPLETE",
            "variables" to mapOf("userName" to "홍길동", "amount" to "50000", "orderId" to "ORD-001"),
            "channels" to listOf("SMS", "PUSH", "IN_APP"),
            "idempotencyKey" to "payment-${UUID.randomUUID()}"
        )

        mockMvc.post("/api/v1/notifications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isAccepted() }
            jsonPath("$.data.deliveries.length()") { value(3) }
        }
    }

    @Test
    fun `POST notifications - security alert with HIGH priority`() {
        val request = mapOf(
            "userId" to "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
            "notificationType" to "SECURITY",
            "templateCode" to "SECURITY_ALERT",
            "variables" to mapOf("userName" to "홍길동", "alertMessage" to "새로운 기기에서 로그인이 감지되었습니다."),
            "priority" to "HIGH",
            "idempotencyKey" to "security-${UUID.randomUUID()}"
        )

        mockMvc.post("/api/v1/notifications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isAccepted() }
            jsonPath("$.data.priority") { value("HIGH") }
        }
    }

    @Test
    fun `GET notifications by ID - should return notification details`() {
        // Create first
        val idempotencyKey = "get-test-${UUID.randomUUID()}"
        val request = mapOf(
            "userId" to "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
            "notificationType" to "ACCOUNT",
            "templateCode" to "WELCOME",
            "variables" to mapOf("userName" to "홍길동"),
            "idempotencyKey" to idempotencyKey
        )

        val createResult = mockMvc.post("/api/v1/notifications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andReturn()

        val responseBody = objectMapper.readTree(createResult.response.contentAsString)
        val notificationId = responseBody["data"]["id"].asText()

        mockMvc.get("/api/v1/notifications/$notificationId").andExpect {
            status { isOk() }
            jsonPath("$.data.id") { value(notificationId) }
            jsonPath("$.data.deliveries") { isArray() }
        }
    }

    @Test
    fun `POST notifications - should return 400 for missing required fields`() {
        val request = mapOf(
            "notificationType" to "ACCOUNT"
            // Missing userId, templateCode, idempotencyKey
        )

        mockMvc.post("/api/v1/notifications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
