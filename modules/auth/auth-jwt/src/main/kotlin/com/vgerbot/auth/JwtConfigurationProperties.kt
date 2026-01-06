package com.vgerbot.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * JWT 配置属性
 * 
 * @property secret JWT 签名密钥（Base64 编码，至少 256 位/32 字节）
 * @property accessTokenExpireTime Access Token 过期时间（秒），默认 1 小时
 * @property refreshTokenExpireTime Refresh Token 过期时间（秒），默认 7 天
 * @property issuer Token 签发者
 */
@ConfigurationProperties(prefix = "jwt", ignoreInvalidFields = false)
data class JwtProperties(
    val secret: String,
    val accessTokenExpireTime: Long = 3600,
    val refreshTokenExpireTime: Long = 604800,
    val issuer: String = "springboot-ktorm-boilerplate"
) {
    companion object {
        const val MIN_SECRET_LENGTH = 32 // 256 bits minimum for HS256
    }
    
    init {
        require(secret.isNotBlank()) { "JWT secret must not be blank" }
        // Base64 解码后检查长度
        val decodedLength = try {
            java.util.Base64.getDecoder().decode(secret).size
        } catch (e: IllegalArgumentException) {
            secret.toByteArray().size
        }
        require(decodedLength >= MIN_SECRET_LENGTH) { 
            "JWT secret must be at least $MIN_SECRET_LENGTH bytes (256 bits) for HS256, current: $decodedLength bytes" 
        }
    }
}

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfigurationProperties
