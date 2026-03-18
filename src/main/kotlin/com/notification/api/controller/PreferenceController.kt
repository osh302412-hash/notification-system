package com.notification.api.controller

import com.notification.api.dto.request.UpdatePreferencesRequest
import com.notification.api.dto.response.ApiResponse
import com.notification.api.dto.response.PreferenceResponse
import com.notification.domain.model.UserNotificationPreference
import com.notification.domain.repository.UserNotificationPreferenceRepository
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users/{userId}/preferences")
class PreferenceController(
    private val preferenceRepository: UserNotificationPreferenceRepository
) {
    @GetMapping
    fun getPreferences(@PathVariable userId: UUID): ResponseEntity<ApiResponse<List<PreferenceResponse>>> {
        val preferences = preferenceRepository.findByUserId(userId)
        val response = preferences.map { PreferenceResponse.from(it) }
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @PutMapping
    fun updatePreferences(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UpdatePreferencesRequest
    ): ResponseEntity<ApiResponse<List<PreferenceResponse>>> {
        val updatedPreferences = request.preferences.map { item ->
            val existing = preferenceRepository.findByUserIdAndNotificationTypeAndChannel(
                userId, item.notificationType, item.channel
            )

            val preference = UserNotificationPreference(
                id = existing?.id,
                userId = userId,
                notificationType = item.notificationType,
                channel = item.channel,
                enabled = item.enabled,
                quietHoursEnabled = item.quietHoursEnabled,
                quietHoursStart = item.quietHoursStart,
                quietHoursEnd = item.quietHoursEnd
            )
            preferenceRepository.save(preference)
        }

        val response = updatedPreferences.map { PreferenceResponse.from(it) }
        return ResponseEntity.ok(ApiResponse.ok(response))
    }
}
