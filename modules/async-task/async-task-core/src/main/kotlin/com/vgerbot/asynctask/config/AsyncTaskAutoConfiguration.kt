package com.vgerbot.asynctask.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * 异步任务自动配置类
 */
@Configuration
@EnableConfigurationProperties(AsyncTaskProperties::class)
@EnableScheduling
@ComponentScan(basePackages = ["com.vgerbot.asynctask"])
@ConditionalOnProperty(prefix = "async-task", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class AsyncTaskAutoConfiguration

