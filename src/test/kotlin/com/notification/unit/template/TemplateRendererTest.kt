package com.notification.unit.template

import com.notification.application.service.TemplateRenderer
import com.notification.common.exception.TemplateNotFoundException
import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.NotificationType
import com.notification.domain.model.NotificationTemplate
import com.notification.domain.repository.NotificationTemplateRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TemplateRendererTest {
    private val templateRepository = mockk<NotificationTemplateRepository>()
    private val renderer = TemplateRenderer(templateRepository)

    @Test
    fun `should render template with variables`() {
        val template = NotificationTemplate(
            code = "WELCOME",
            notificationType = NotificationType.ACCOUNT,
            channel = NotificationChannel.EMAIL,
            titleTemplate = "{{userName}}님, 환영합니다!",
            bodyTemplate = "안녕하세요 {{userName}}님, 회원가입을 축하합니다."
        )
        every { templateRepository.findActiveByCodeAndChannel("WELCOME", NotificationChannel.EMAIL) } returns template

        val result = renderer.render("WELCOME", NotificationChannel.EMAIL, mapOf("userName" to "홍길동"))

        assertEquals("홍길동님, 환영합니다!", result.title)
        assertEquals("안녕하세요 홍길동님, 회원가입을 축하합니다.", result.body)
    }

    @Test
    fun `should use default variables when not provided`() {
        val template = NotificationTemplate(
            code = "TEST",
            notificationType = NotificationType.ACCOUNT,
            channel = NotificationChannel.EMAIL,
            titleTemplate = "Hello {{userName}}",
            bodyTemplate = "Welcome to {{serviceName}}",
            defaultVariables = mapOf("serviceName" to "TestService")
        )
        every { templateRepository.findActiveByCodeAndChannel("TEST", NotificationChannel.EMAIL) } returns template

        val result = renderer.render("TEST", NotificationChannel.EMAIL, mapOf("userName" to "Test"))

        assertEquals("Hello Test", result.title)
        assertEquals("Welcome to TestService", result.body)
    }

    @Test
    fun `should override default variables with provided ones`() {
        val template = NotificationTemplate(
            code = "TEST",
            notificationType = NotificationType.ACCOUNT,
            channel = NotificationChannel.EMAIL,
            titleTemplate = "{{greeting}}",
            bodyTemplate = "Body",
            defaultVariables = mapOf("greeting" to "Default")
        )
        every { templateRepository.findActiveByCodeAndChannel("TEST", NotificationChannel.EMAIL) } returns template

        val result = renderer.render("TEST", NotificationChannel.EMAIL, mapOf("greeting" to "Custom"))

        assertEquals("Custom", result.title)
    }

    @Test
    fun `should throw TemplateNotFoundException when template not found`() {
        every { templateRepository.findActiveByCodeAndChannel("MISSING", NotificationChannel.EMAIL) } returns null

        assertThrows<TemplateNotFoundException> {
            renderer.render("MISSING", NotificationChannel.EMAIL, emptyMap())
        }
    }

    @Test
    fun `should handle template with no variables`() {
        val template = NotificationTemplate(
            code = "STATIC",
            notificationType = NotificationType.ACCOUNT,
            channel = NotificationChannel.EMAIL,
            titleTemplate = "Static Title",
            bodyTemplate = "Static Body"
        )
        every { templateRepository.findActiveByCodeAndChannel("STATIC", NotificationChannel.EMAIL) } returns template

        val result = renderer.render("STATIC", NotificationChannel.EMAIL, emptyMap())

        assertEquals("Static Title", result.title)
        assertEquals("Static Body", result.body)
    }

    @Test
    fun `should return template version in rendered content`() {
        val template = NotificationTemplate(
            code = "VER",
            version = 3,
            notificationType = NotificationType.ACCOUNT,
            channel = NotificationChannel.EMAIL,
            titleTemplate = "Title",
            bodyTemplate = "Body"
        )
        every { templateRepository.findActiveByCodeAndChannel("VER", NotificationChannel.EMAIL) } returns template

        val result = renderer.render("VER", NotificationChannel.EMAIL, emptyMap())

        assertEquals(3, result.templateVersion)
    }
}
