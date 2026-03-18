package com.notification.integration.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.notification.integration.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.util.UUID

@AutoConfigureMockMvc
class InAppNotificationApiTest : BaseIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val userId = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"

    @Test
    fun `should create and retrieve in-app notifications`() {
        // Create a notification that includes IN_APP
        val request = mapOf(
            "userId" to userId,
            "notificationType" to "ACCOUNT",
            "templateCode" to "WELCOME",
            "variables" to mapOf("userName" to "홍길동"),
            "channels" to listOf("IN_APP"),
            "idempotencyKey" to "inapp-test-${UUID.randomUUID()}"
        )

        mockMvc.post("/api/v1/notifications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isAccepted() }
        }

        // Wait briefly for worker to process
        Thread.sleep(2000)

        // Retrieve in-app notifications
        mockMvc.get("/api/v1/users/$userId/in-app-notifications").andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.notifications") { isArray() }
        }
    }

    @Test
    fun `should mark in-app notification as read`() {
        // Create notification
        val request = mapOf(
            "userId" to userId,
            "notificationType" to "ACCOUNT",
            "templateCode" to "WELCOME",
            "variables" to mapOf("userName" to "홍길동"),
            "channels" to listOf("IN_APP"),
            "idempotencyKey" to "inapp-read-${UUID.randomUUID()}"
        )

        mockMvc.post("/api/v1/notifications") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }

        Thread.sleep(2000)

        // Get notifications to find the ID
        val listResult = mockMvc.get("/api/v1/users/$userId/in-app-notifications").andReturn()
        val listBody = objectMapper.readTree(listResult.response.contentAsString)
        val notifications = listBody["data"]["notifications"]

        if (notifications.size() > 0) {
            val notificationId = notifications[0]["id"].asText()

            mockMvc.patch("/api/v1/users/$userId/in-app-notifications/$notificationId/read") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.status") { value("read") }
            }
        }
    }
}
