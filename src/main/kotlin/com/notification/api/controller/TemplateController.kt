package com.notification.api.controller

import com.notification.api.dto.request.RenderPreviewRequest
import com.notification.api.dto.response.ApiResponse
import com.notification.api.dto.response.RenderedTemplateResponse
import com.notification.api.dto.response.TemplateResponse
import com.notification.application.service.TemplateRenderer
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/templates")
class TemplateController(
    private val templateRenderer: TemplateRenderer
) {
    @GetMapping("/{code}")
    fun getTemplate(@PathVariable code: String): ResponseEntity<ApiResponse<List<TemplateResponse>>> {
        val templates = templateRenderer.getTemplate(code)
        if (templates.isEmpty()) {
            return ResponseEntity.notFound().build()
        }
        val response = templates.map { TemplateResponse.from(it) }
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @PostMapping("/{code}/render-preview")
    fun renderPreview(
        @PathVariable code: String,
        @Valid @RequestBody request: RenderPreviewRequest
    ): ResponseEntity<ApiResponse<RenderedTemplateResponse>> {
        val rendered = templateRenderer.renderPreview(code, request.channel, request.variables)
        val response = RenderedTemplateResponse(
            title = rendered.title,
            body = rendered.body,
            templateVersion = rendered.templateVersion
        )
        return ResponseEntity.ok(ApiResponse.ok(response))
    }
}
