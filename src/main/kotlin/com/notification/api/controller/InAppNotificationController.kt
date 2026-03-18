package com.notification.api.controller

import com.notification.api.dto.response.ApiResponse
import com.notification.api.dto.response.InAppNotificationListResponse
import com.notification.api.dto.response.InAppNotificationResponse
import com.notification.domain.repository.InAppNotificationRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users/{userId}/in-app-notifications")
class InAppNotificationController(
    private val inAppNotificationRepository: InAppNotificationRepository
) {
    @GetMapping
    fun getInAppNotifications(
        @PathVariable userId: UUID,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<ApiResponse<InAppNotificationListResponse>> {
        val notifications = inAppNotificationRepository.findByUserId(userId, limit, offset)
        val unreadCount = inAppNotificationRepository.countUnreadByUserId(userId)

        val response = InAppNotificationListResponse(
            notifications = notifications.map { InAppNotificationResponse.from(it) },
            unreadCount = unreadCount
        )

        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @PatchMapping("/{notificationId}/read")
    fun markAsRead(
        @PathVariable userId: UUID,
        @PathVariable notificationId: UUID
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        val notification = inAppNotificationRepository.findById(notificationId)
            ?: return ResponseEntity.notFound().build()

        if (notification.userId != userId) {
            return ResponseEntity.status(403).body(
                ApiResponse.error("FORBIDDEN", "Not authorized to modify this notification")
            )
        }

        inAppNotificationRepository.markAsRead(notificationId)
        return ResponseEntity.ok(ApiResponse.ok(mapOf("status" to "read")))
    }
}
