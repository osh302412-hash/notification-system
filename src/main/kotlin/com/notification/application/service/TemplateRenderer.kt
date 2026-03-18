package com.notification.application.service

import com.notification.common.exception.TemplateNotFoundException
import com.notification.common.exception.TemplateRenderingException
import com.notification.domain.model.NotificationChannel
import com.notification.domain.model.NotificationTemplate
import com.notification.domain.repository.NotificationTemplateRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

data class RenderedContent(
    val title: String,
    val body: String,
    val templateVersion: Int
)

@Service
class TemplateRenderer(
    private val templateRepository: NotificationTemplateRepository
) {
    fun render(templateCode: String, channel: NotificationChannel, variables: Map<String, String>): RenderedContent {
        val template = templateRepository.findActiveByCodeAndChannel(templateCode, channel)
            ?: throw TemplateNotFoundException(templateCode, channel.name)

        return try {
            val title = replaceVariables(template.titleTemplate, variables, template.defaultVariables)
            val body = replaceVariables(template.bodyTemplate, variables, template.defaultVariables)
            RenderedContent(title = title, body = body, templateVersion = template.version)
        } catch (e: Exception) {
            if (e is TemplateNotFoundException) throw e
            throw TemplateRenderingException(templateCode, e)
        }
    }

    fun renderPreview(templateCode: String, channel: NotificationChannel, variables: Map<String, String>): RenderedContent {
        return render(templateCode, channel, variables)
    }

    fun getTemplate(code: String): List<NotificationTemplate> {
        return templateRepository.findByCode(code)
    }

    private fun replaceVariables(
        template: String,
        variables: Map<String, String>,
        defaults: Map<String, String>
    ): String {
        val merged = defaults + variables
        var result = template
        merged.forEach { (key, value) ->
            result = result.replace("{{$key}}", value)
        }
        // Check for unreplaced variables
        val unreplaced = Regex("\\{\\{(\\w+)}}").findAll(result).map { it.groupValues[1] }.toList()
        if (unreplaced.isNotEmpty()) {
            logger.warn { "Unreplaced template variables: $unreplaced in template" }
        }
        return result
    }
}
