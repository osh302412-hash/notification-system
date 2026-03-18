package com.notification.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "notification")
class NotificationProperties {
    var worker: WorkerProperties = WorkerProperties()
    var retry: RetryProperties = RetryProperties()
    var idempotency: IdempotencyProperties = IdempotencyProperties()
    var quietHours: QuietHoursProperties = QuietHoursProperties()
    var deduplication: DeduplicationProperties = DeduplicationProperties()
}

class WorkerProperties {
    var pollIntervalMs: Long = 1000
    var batchSize: Int = 50
    var threadPoolSize: Int = 5
}

class RetryProperties {
    var maxAttempts: Int = 3
    var initialDelayMs: Long = 1000
    var multiplier: Double = 2.0
    var maxDelayMs: Long = 30000
}

class IdempotencyProperties {
    var ttlHours: Long = 24
}

class QuietHoursProperties {
    var defaultStart: String = "22:00"
    var defaultEnd: String = "08:00"
}

class DeduplicationProperties {
    var windowMinutes: Long = 60
}
