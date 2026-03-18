package com.notification.unit.preference

import com.notification.application.service.PreferenceResolver
import com.notification.domain.model.*
import com.notification.domain.repository.UserNotificationPreferenceRepository
import com.notification.infrastructure.config.NotificationProperties
import com.notification.infrastructure.config.QuietHoursProperties
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalTime
import java.util.UUID

class PreferenceResolverTest {
    private val preferenceRepository = mockk<UserNotificationPreferenceRepository>()
    private val properties = NotificationProperties().apply {
        quietHours = QuietHoursProperties().apply {
            defaultStart = "22:00"
            defaultEnd = "08:00"
        }
    }
    private val resolver = PreferenceResolver(preferenceRepository, properties)
    private val userId = UUID.randomUUID()

    @Test
    fun `should allow channel when preference is enabled`() {
        every {
            preferenceRepository.findByUserIdAndNotificationTypeAndChannel(
                userId, NotificationType.ACCOUNT, NotificationChannel.EMAIL
            )
        } returns UserNotificationPreference(
            userId = userId,
            notificationType = NotificationType.ACCOUNT,
            channel = NotificationChannel.EMAIL,
            enabled = true
        )

        val result = resolver.resolveChannel(userId, NotificationType.ACCOUNT, NotificationChannel.EMAIL, NotificationPriority.NORMAL)

        assertTrue(result.allowed)
        assertNull(result.blockedReason)
    }

    @Test
    fun `should block channel when preference is disabled`() {
        every {
            preferenceRepository.findByUserIdAndNotificationTypeAndChannel(
                userId, NotificationType.MARKETING, NotificationChannel.EMAIL
            )
        } returns UserNotificationPreference(
            userId = userId,
            notificationType = NotificationType.MARKETING,
            channel = NotificationChannel.EMAIL,
            enabled = false
        )

        val result = resolver.resolveChannel(userId, NotificationType.MARKETING, NotificationChannel.EMAIL, NotificationPriority.NORMAL)

        assertFalse(result.allowed)
        assertEquals("Channel disabled by user", result.blockedReason)
    }

    @Test
    fun `should block marketing when no preference exists`() {
        every {
            preferenceRepository.findByUserIdAndNotificationTypeAndChannel(
                userId, NotificationType.MARKETING, NotificationChannel.EMAIL
            )
        } returns null

        val result = resolver.resolveChannel(userId, NotificationType.MARKETING, NotificationChannel.EMAIL, NotificationPriority.NORMAL)

        assertFalse(result.allowed)
        assertEquals("Marketing requires explicit consent", result.blockedReason)
    }

    @Test
    fun `should allow non-marketing when no preference exists`() {
        every {
            preferenceRepository.findByUserIdAndNotificationTypeAndChannel(
                userId, NotificationType.ACCOUNT, NotificationChannel.EMAIL
            )
        } returns null

        val result = resolver.resolveChannel(userId, NotificationType.ACCOUNT, NotificationChannel.EMAIL, NotificationPriority.NORMAL)

        assertTrue(result.allowed)
    }

    @Test
    fun `should block during quiet hours`() {
        val pref = UserNotificationPreference(
            userId = userId,
            notificationType = NotificationType.MARKETING,
            channel = NotificationChannel.PUSH,
            enabled = true,
            quietHoursEnabled = true,
            quietHoursStart = LocalTime.of(0, 0),
            quietHoursEnd = LocalTime.of(23, 59)
        )

        assertTrue(resolver.isInQuietHours(pref, "Asia/Seoul"))
    }

    @Test
    fun `should bypass quiet hours for HIGH priority`() {
        every {
            preferenceRepository.findByUserIdAndNotificationTypeAndChannel(
                userId, NotificationType.SECURITY, NotificationChannel.EMAIL
            )
        } returns UserNotificationPreference(
            userId = userId,
            notificationType = NotificationType.SECURITY,
            channel = NotificationChannel.EMAIL,
            enabled = true,
            quietHoursEnabled = true,
            quietHoursStart = LocalTime.of(0, 0),
            quietHoursEnd = LocalTime.of(23, 59)
        )

        val result = resolver.resolveChannel(userId, NotificationType.SECURITY, NotificationChannel.EMAIL, NotificationPriority.HIGH)

        assertTrue(result.allowed)
    }

    @Test
    fun `should bypass quiet hours for URGENT priority`() {
        every {
            preferenceRepository.findByUserIdAndNotificationTypeAndChannel(
                userId, NotificationType.SECURITY, NotificationChannel.SMS
            )
        } returns UserNotificationPreference(
            userId = userId,
            notificationType = NotificationType.SECURITY,
            channel = NotificationChannel.SMS,
            enabled = true,
            quietHoursEnabled = true,
            quietHoursStart = LocalTime.of(0, 0),
            quietHoursEnd = LocalTime.of(23, 59)
        )

        val result = resolver.resolveChannel(userId, NotificationType.SECURITY, NotificationChannel.SMS, NotificationPriority.URGENT)

        assertTrue(result.allowed)
    }

    @Test
    fun `should not block when quiet hours are disabled`() {
        val pref = UserNotificationPreference(
            userId = userId,
            notificationType = NotificationType.MARKETING,
            channel = NotificationChannel.PUSH,
            enabled = true,
            quietHoursEnabled = false,
            quietHoursStart = LocalTime.of(0, 0),
            quietHoursEnd = LocalTime.of(23, 59)
        )

        assertFalse(resolver.isInQuietHours(pref, "Asia/Seoul"))
    }
}
