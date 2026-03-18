package com.notification.integration.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.notification.integration.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put

@AutoConfigureMockMvc
class PreferenceApiTest : BaseIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val userId = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"

    @Test
    fun `should get user preferences`() {
        mockMvc.get("/api/v1/users/$userId/preferences").andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data") { isArray() }
        }
    }

    @Test
    fun `should update user preferences`() {
        val request = mapOf(
            "preferences" to listOf(
                mapOf(
                    "notificationType" to "MARKETING",
                    "channel" to "EMAIL",
                    "enabled" to false,
                    "quietHoursEnabled" to false
                )
            )
        )

        mockMvc.put("/api/v1/users/$userId/preferences") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data[0].enabled") { value(false) }
        }
    }
}
