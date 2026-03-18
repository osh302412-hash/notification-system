package com.notification.domain.model

data class ProviderResult(
    val success: Boolean,
    val provider: String,
    val messageId: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val retryable: Boolean = false,
    val responsePayload: Map<String, Any>? = null,
    val durationMs: Long = 0
)
