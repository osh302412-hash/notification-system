package com.notification.api.exception

import com.notification.api.dto.response.ApiResponse
import com.notification.common.exception.*
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateRequestException::class)
    fun handleDuplicateRequest(e: DuplicateRequestException): ResponseEntity<ApiResponse<Nothing>> {
        logger.info { "Duplicate request: ${e.message}" }
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error("DUPLICATE_REQUEST", e.message ?: "Duplicate request"))
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(e: UserNotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("USER_NOT_FOUND", e.message ?: "User not found"))
    }

    @ExceptionHandler(TemplateNotFoundException::class)
    fun handleTemplateNotFound(e: TemplateNotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("TEMPLATE_NOT_FOUND", e.message ?: "Template not found"))
    }

    @ExceptionHandler(TemplateRenderingException::class)
    fun handleTemplateRendering(e: TemplateRenderingException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error(e) { "Template rendering error" }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("TEMPLATE_RENDERING_ERROR", e.message ?: "Template rendering failed"))
    }

    @ExceptionHandler(PreferenceBlockedException::class)
    fun handlePreferenceBlocked(e: PreferenceBlockedException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiResponse.error("PREFERENCE_BLOCKED", e.message ?: "Blocked by preference"))
    }

    @ExceptionHandler(InvalidChannelException::class)
    fun handleInvalidChannel(e: InvalidChannelException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("INVALID_CHANNEL", e.message ?: "Invalid channel"))
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(e: ValidationException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("VALIDATION_ERROR", e.message ?: "Validation failed"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errors = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("VALIDATION_ERROR", errors))
    }

    @ExceptionHandler(NotificationException::class)
    fun handleNotificationException(e: NotificationException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error(e) { "Notification error: ${e.message}" }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("NOTIFICATION_ERROR", e.message ?: "Unknown error"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error(e) { "Unexpected error" }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"))
    }
}
