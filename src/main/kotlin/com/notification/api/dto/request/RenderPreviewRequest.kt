package com.notification.api.dto.request

import com.notification.domain.model.NotificationChannel

data class RenderPreviewRequest(
    val channel: NotificationChannel,
    val variables: Map<String, String> = emptyMap()
)
