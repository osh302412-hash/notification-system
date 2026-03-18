package com.notification

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import com.notification.infrastructure.config.NotificationProperties

@SpringBootApplication
@EnableConfigurationProperties(NotificationProperties::class)
class NotificationSystemApplication

fun main(args: Array<String>) {
    runApplication<NotificationSystemApplication>(*args)
}
