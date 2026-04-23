package com.notification.infrastructure.persistence.mapper

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.notification.domain.model.*
import com.notification.infrastructure.persistence.entity.*
import java.util.UUID

object EntityMapper {
    private val objectMapper = jacksonObjectMapper()

    // User
    fun toEntity(domain: User): UserEntity = UserEntity(
        id = domain.id,
        email = domain.email,
        phoneNumber = domain.phoneNumber,
        deviceToken = domain.deviceToken,
        name = domain.name,
        timezone = domain.timezone,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt
    )

    fun toDomain(entity: UserEntity): User = User(
        id = entity.id,
        email = entity.email,
        phoneNumber = entity.phoneNumber,
        deviceToken = entity.deviceToken,
        name = entity.name,
        timezone = entity.timezone,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    // NotificationTemplate
    fun toEntity(domain: NotificationTemplate): NotificationTemplateEntity = NotificationTemplateEntity(
        id = domain.id ?: UUID.randomUUID(),
        code = domain.code,
        version = domain.version,
        notificationType = domain.notificationType.name,
        channel = domain.channel.name,
        titleTemplate = domain.titleTemplate,
        bodyTemplate = domain.bodyTemplate,
        defaultVariables = objectMapper.writeValueAsString(domain.defaultVariables),
        active = domain.active,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt
    )

    fun toDomain(entity: NotificationTemplateEntity): NotificationTemplate = NotificationTemplate(
        id = entity.id,
        code = entity.code,
        version = entity.version,
        notificationType = NotificationType.valueOf(entity.notificationType),
        channel = NotificationChannel.valueOf(entity.channel),
        titleTemplate = entity.titleTemplate,
        bodyTemplate = entity.bodyTemplate,
        defaultVariables = objectMapper.readValue(entity.defaultVariables, object : TypeReference<Map<String, String>>() {}),
        active = entity.active,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    // UserNotificationPreference
    fun toEntity(domain: UserNotificationPreference): UserNotificationPreferenceEntity = UserNotificationPreferenceEntity(
        id = domain.id ?: UUID.randomUUID(),
        userId = domain.userId,
        notificationType = domain.notificationType.name,
        channel = domain.channel.name,
        enabled = domain.enabled,
        quietHoursStart = domain.quietHoursStart,
        quietHoursEnd = domain.quietHoursEnd,
        quietHoursEnabled = domain.quietHoursEnabled,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt
    )

    fun toDomain(entity: UserNotificationPreferenceEntity): UserNotificationPreference = UserNotificationPreference(
        id = entity.id,
        userId = entity.userId,
        notificationType = NotificationType.valueOf(entity.notificationType),
        channel = NotificationChannel.valueOf(entity.channel),
        enabled = entity.enabled,
        quietHoursStart = entity.quietHoursStart,
        quietHoursEnd = entity.quietHoursEnd,
        quietHoursEnabled = entity.quietHoursEnabled,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    // NotificationRequest
    fun toEntity(domain: NotificationRequest): NotificationRequestEntity = NotificationRequestEntity(
        id = domain.id ?: UUID.randomUUID(),
        idempotencyKey = domain.idempotencyKey,
        userId = domain.userId,
        notificationType = domain.notificationType.name,
        templateCode = domain.templateCode,
        variables = objectMapper.writeValueAsString(domain.variables),
        requestedChannels = domain.requestedChannels.map { it.name }.toTypedArray(),
        resolvedChannels = domain.resolvedChannels?.map { it.name }?.toTypedArray(),
        priority = domain.priority.name,
        status = domain.status.name,
        scheduledAt = domain.scheduledAt,
        preferenceSnapshot = domain.preferenceSnapshot?.let { objectMapper.writeValueAsString(it) },
        correlationId = domain.correlationId,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt
    )

    fun toDomain(entity: NotificationRequestEntity): NotificationRequest = NotificationRequest(
        id = entity.id,
        idempotencyKey = entity.idempotencyKey,
        userId = entity.userId,
        notificationType = NotificationType.valueOf(entity.notificationType),
        templateCode = entity.templateCode,
        variables = objectMapper.readValue(entity.variables, object : TypeReference<Map<String, String>>() {}),
        requestedChannels = entity.requestedChannels.mapNotNull { runCatching { NotificationChannel.valueOf(it) }.getOrNull() },
        resolvedChannels = entity.resolvedChannels?.mapNotNull { runCatching { NotificationChannel.valueOf(it) }.getOrNull() },
        priority = NotificationPriority.valueOf(entity.priority),
        status = NotificationRequestStatus.valueOf(entity.status),
        scheduledAt = entity.scheduledAt,
        preferenceSnapshot = entity.preferenceSnapshot?.let { objectMapper.readValue(it, object : TypeReference<Map<String, Any>>() {}) },
        correlationId = entity.correlationId,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    // NotificationDelivery
    fun toEntity(domain: NotificationDelivery): NotificationDeliveryEntity = NotificationDeliveryEntity(
        id = domain.id ?: UUID.randomUUID(),
        notificationRequestId = domain.notificationRequestId,
        channel = domain.channel.name,
        provider = domain.provider,
        status = domain.status.name,
        dedupKey = domain.dedupKey,
        attemptCount = domain.attemptCount,
        maxAttempts = domain.maxAttempts,
        nextRetryAt = domain.nextRetryAt,
        lastAttemptedAt = domain.lastAttemptedAt,
        completedAt = domain.completedAt,
        errorCode = domain.errorCode,
        errorMessage = domain.errorMessage,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt
    )

    fun toDomain(entity: NotificationDeliveryEntity): NotificationDelivery = NotificationDelivery(
        id = entity.id,
        notificationRequestId = entity.notificationRequestId,
        channel = NotificationChannel.valueOf(entity.channel),
        provider = entity.provider,
        status = DeliveryStatus.valueOf(entity.status),
        dedupKey = entity.dedupKey,
        attemptCount = entity.attemptCount,
        maxAttempts = entity.maxAttempts,
        nextRetryAt = entity.nextRetryAt,
        lastAttemptedAt = entity.lastAttemptedAt,
        completedAt = entity.completedAt,
        errorCode = entity.errorCode,
        errorMessage = entity.errorMessage,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    // NotificationDeliveryAttempt
    fun toEntity(domain: NotificationDeliveryAttempt): NotificationDeliveryAttemptEntity = NotificationDeliveryAttemptEntity(
        id = domain.id ?: UUID.randomUUID(),
        deliveryId = domain.deliveryId,
        attemptNumber = domain.attemptNumber,
        provider = domain.provider,
        status = domain.status.name,
        requestPayload = domain.requestPayload?.let { objectMapper.writeValueAsString(it) },
        responsePayload = domain.responsePayload?.let { objectMapper.writeValueAsString(it) },
        errorCode = domain.errorCode,
        errorMessage = domain.errorMessage,
        durationMs = domain.durationMs,
        createdAt = domain.createdAt
    )

    fun toDomain(entity: NotificationDeliveryAttemptEntity): NotificationDeliveryAttempt = NotificationDeliveryAttempt(
        id = entity.id,
        deliveryId = entity.deliveryId,
        attemptNumber = entity.attemptNumber,
        provider = entity.provider,
        status = DeliveryAttemptStatus.valueOf(entity.status),
        requestPayload = entity.requestPayload?.let { objectMapper.readValue(it, object : TypeReference<Map<String, Any>>() {}) },
        responsePayload = entity.responsePayload?.let { objectMapper.readValue(it, object : TypeReference<Map<String, Any>>() {}) },
        errorCode = entity.errorCode,
        errorMessage = entity.errorMessage,
        durationMs = entity.durationMs,
        createdAt = entity.createdAt
    )

    // InAppNotification
    fun toEntity(domain: InAppNotification): InAppNotificationEntity = InAppNotificationEntity(
        id = domain.id ?: UUID.randomUUID(),
        userId = domain.userId,
        notificationRequestId = domain.notificationRequestId,
        title = domain.title,
        body = domain.body,
        notificationType = domain.notificationType.name,
        data = objectMapper.writeValueAsString(domain.data),
        read = domain.read,
        readAt = domain.readAt,
        createdAt = domain.createdAt
    )

    fun toDomain(entity: InAppNotificationEntity): InAppNotification = InAppNotification(
        id = entity.id,
        userId = entity.userId,
        notificationRequestId = entity.notificationRequestId,
        title = entity.title,
        body = entity.body,
        notificationType = NotificationType.valueOf(entity.notificationType),
        data = objectMapper.readValue(entity.data, object : TypeReference<Map<String, Any>>() {}),
        read = entity.read,
        readAt = entity.readAt,
        createdAt = entity.createdAt
    )

    // IdempotencyRecord
    fun toEntity(domain: IdempotencyRecord): IdempotencyRecordEntity = IdempotencyRecordEntity(
        id = domain.id ?: UUID.randomUUID(),
        idempotencyKey = domain.idempotencyKey,
        notificationRequestId = domain.notificationRequestId,
        status = domain.status,
        createdAt = domain.createdAt,
        expiresAt = domain.expiresAt
    )

    fun toDomain(entity: IdempotencyRecordEntity): IdempotencyRecord = IdempotencyRecord(
        id = entity.id,
        idempotencyKey = entity.idempotencyKey,
        notificationRequestId = entity.notificationRequestId,
        status = entity.status,
        createdAt = entity.createdAt,
        expiresAt = entity.expiresAt
    )

}
