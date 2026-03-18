package com.notification.api.controller

import com.notification.api.dto.request.SendNotificationRequest
import com.notification.api.dto.response.ApiResponse
import com.notification.api.dto.response.DeliveryResponse
import com.notification.api.dto.response.NotificationResponse
import com.notification.application.orchestrator.NotificationOrchestrator
import com.notification.domain.repository.NotificationDeliveryRepository
import com.notification.domain.repository.NotificationRequestRepository
import jakarta.validation.Valid
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val orchestrator: NotificationOrchestrator,
    private val requestRepository: NotificationRequestRepository,
    private val deliveryRepository: NotificationDeliveryRepository
) {
    @PostMapping
    fun sendNotification(
        @Valid @RequestBody request: SendNotificationRequest
    ): ResponseEntity<ApiResponse<NotificationResponse>> {
        val correlationId = MDC.get("correlationId") ?: UUID.randomUUID().toString()

        val notificationRequest = orchestrator.createNotification(
            userId = request.userId,
            notificationType = request.notificationType,
            templateCode = request.templateCode,
            variables = request.variables,
            channels = request.channels,
            priority = request.priority,
            idempotencyKey = request.idempotencyKey,
            scheduledAt = request.scheduledAt,
            correlationId = correlationId
        )

        val deliveries = deliveryRepository.findByNotificationRequestId(notificationRequest.id!!)
        val response = NotificationResponse.from(notificationRequest, deliveries)

        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.ok(response))
    }

    @GetMapping("/{id}")
    fun getNotification(@PathVariable id: UUID): ResponseEntity<ApiResponse<NotificationResponse>> {
        val request = requestRepository.findById(id)
            ?: return ResponseEntity.notFound().build()

        val deliveries = deliveryRepository.findByNotificationRequestId(id)
        val response = NotificationResponse.from(request, deliveries)

        return ResponseEntity.ok(ApiResponse.ok(response))
    }
}
