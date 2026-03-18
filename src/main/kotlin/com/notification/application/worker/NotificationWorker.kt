package com.notification.application.worker

import com.notification.application.orchestrator.DeliveryProcessor
import com.notification.domain.model.DeliveryStatus
import com.notification.domain.repository.NotificationDeliveryRepository
import com.notification.domain.repository.NotificationRequestRepository
import com.notification.domain.model.NotificationRequestStatus
import com.notification.infrastructure.config.NotificationProperties
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}

@Component
class NotificationWorker(
    private val deliveryRepository: NotificationDeliveryRepository,
    private val requestRepository: NotificationRequestRepository,
    private val deliveryProcessor: DeliveryProcessor,
    private val properties: NotificationProperties
) {
    @Scheduled(fixedDelayString = "\${notification.worker.poll-interval-ms:1000}")
    fun processPendingDeliveries() {
        try {
            // 1. Mark pending requests as processing
            val pendingRequests = requestRepository.findByStatus(
                NotificationRequestStatus.PENDING, properties.worker.batchSize
            )
            pendingRequests.forEach { request ->
                requestRepository.updateStatus(request.id!!, NotificationRequestStatus.PROCESSING)
            }

            // 2. Fetch pending deliveries
            val deliveries = deliveryRepository.findPendingDeliveries(properties.worker.batchSize)

            if (deliveries.isNotEmpty()) {
                logger.debug { "Processing ${deliveries.size} pending deliveries" }
            }

            deliveries.forEach { delivery ->
                try {
                    deliveryProcessor.processDelivery(delivery)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to process delivery ${delivery.id}" }
                    deliveryRepository.updateStatus(
                        delivery.id!!, DeliveryStatus.FAILED,
                        errorCode = "PROCESSING_ERROR",
                        errorMessage = e.message
                    )
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Worker error in processPendingDeliveries" }
        }
    }

    @Scheduled(fixedDelayString = "\${notification.worker.poll-interval-ms:2000}")
    fun processRetryDeliveries() {
        try {
            val retryDeliveries = deliveryRepository.findByStatusAndNextRetryBefore(
                DeliveryStatus.RETRY_PENDING,
                ZonedDateTime.now(),
                properties.worker.batchSize
            )

            if (retryDeliveries.isNotEmpty()) {
                logger.debug { "Processing ${retryDeliveries.size} retry deliveries" }
            }

            retryDeliveries.forEach { delivery ->
                try {
                    // Reset status to PENDING for reprocessing
                    deliveryRepository.updateStatus(delivery.id!!, DeliveryStatus.SENDING)
                    deliveryProcessor.processDelivery(delivery)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to process retry delivery ${delivery.id}" }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Worker error in processRetryDeliveries" }
        }
    }
}
