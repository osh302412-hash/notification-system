package com.notification.common.exception

import java.util.UUID

open class NotificationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class TemplateNotFoundException(code: String, channel: String) :
    NotificationException("Template not found: code=$code, channel=$channel")

class TemplateRenderingException(code: String, cause: Throwable? = null) :
    NotificationException("Failed to render template: $code", cause)

class UserNotFoundException(userId: UUID) :
    NotificationException("User not found: $userId")

class PreferenceBlockedException(userId: UUID, channel: String, reason: String) :
    NotificationException("Notification blocked for user=$userId, channel=$channel: $reason")

class DuplicateRequestException(idempotencyKey: String, existingRequestId: UUID) :
    NotificationException("Duplicate request: idempotencyKey=$idempotencyKey, existingRequestId=$existingRequestId")

class ProviderException(provider: String, message: String, val retryable: Boolean = false, cause: Throwable? = null) :
    NotificationException("Provider error [$provider]: $message", cause)

class InvalidChannelException(channel: String) :
    NotificationException("Unsupported channel: $channel")

class QuietHoursBlockedException(userId: UUID, channel: String) :
    NotificationException("Quiet hours active for user=$userId, channel=$channel")

class DeliveryExhaustedException(deliveryId: UUID) :
    NotificationException("All retry attempts exhausted for delivery=$deliveryId")

class ValidationException(message: String) :
    NotificationException("Validation error: $message")
