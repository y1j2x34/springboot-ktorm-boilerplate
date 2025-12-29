package com.vgerbot.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration


@ConfigurationProperties(prefix = "jwt", ignoreInvalidFields = false)
data class JwtProperties(
    val secret: String
)

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfigurationProperties
