package com.vgerbot.postgrest.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.ComponentScan

/**
 * PostgREST 查询自动配置
 * 自动扫描并注册相关组件
 */
@AutoConfiguration
@ConditionalOnClass(PostgrestQueryAutoConfiguration::class)
@ComponentScan(basePackages = ["com.vgerbot.postgrest"])
class PostgrestQueryAutoConfiguration

