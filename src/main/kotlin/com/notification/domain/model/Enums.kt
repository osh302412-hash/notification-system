package com.notification.domain.model

enum class NotificationChannel {
    EMAIL, SMS, PUSH, IN_APP
}

enum class NotificationType {
    ACCOUNT, TRANSACTION, SECURITY, MARKETING
}

enum class NotificationPriority {
    LOW, NORMAL, HIGH, URGENT
}

enum class NotificationRequestStatus {
    PENDING,
    PROCESSING,
    PARTIALLY_COMPLETED,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class DeliveryStatus {
    PENDING,
    SENDING,
    SENT,
    DELIVERED,
    FAILED,
    RETRY_PENDING,
    DEAD_LETTER,
    SKIPPED
}

enum class DeliveryAttemptStatus {
    SUCCESS, FAILED, TIMEOUT, ERROR
}
