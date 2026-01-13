package com.vgerbot.notification.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * 通知模块自动配置
 */
@Configuration
@ConditionalOnProperty(
    prefix = "notification",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(NotificationProperties::class)
@ComponentScan(basePackages = ["com.vgerbot.notification"])
class NotificationAutoConfiguration

