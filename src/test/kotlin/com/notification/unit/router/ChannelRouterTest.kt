package com.notification.unit.router

import com.notification.application.service.ChannelRouter
import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.NotificationType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ChannelRouterTest {
    private val router = ChannelRouter()

    @Test
    fun `should use requested channels when provided`() {
        val channels = listOf(NotificationChannel.EMAIL, NotificationChannel.SMS)
        val result = router.resolveChannels(NotificationType.ACCOUNT, channels)
        assertEquals(channels, result)
    }

    @Test
    fun `should use default channels for ACCOUNT type`() {
        val result = router.resolveChannels(NotificationType.ACCOUNT, null)
        assertEquals(listOf(NotificationChannel.EMAIL, NotificationChannel.IN_APP), result)
    }

    @Test
    fun `should use default channels for TRANSACTION type`() {
        val result = router.resolveChannels(NotificationType.TRANSACTION, null)
        assertEquals(listOf(NotificationChannel.SMS, NotificationChannel.PUSH, NotificationChannel.IN_APP), result)
    }

    @Test
    fun `should use default channels for SECURITY type`() {
        val result = router.resolveChannels(NotificationType.SECURITY, null)
        assertEquals(
            listOf(NotificationChannel.EMAIL, NotificationChannel.SMS, NotificationChannel.PUSH, NotificationChannel.IN_APP),
            result
        )
    }

    @Test
    fun `should use default channels for MARKETING type`() {
        val result = router.resolveChannels(NotificationType.MARKETING, null)
        assertEquals(listOf(NotificationChannel.EMAIL, NotificationChannel.PUSH, NotificationChannel.IN_APP), result)
    }

    @Test
    fun `should use requested channels when empty list provided`() {
        val result = router.resolveChannels(NotificationType.ACCOUNT, emptyList())
        // Empty list = use defaults
        assertEquals(listOf(NotificationChannel.EMAIL, NotificationChannel.IN_APP), result)
    }
}
