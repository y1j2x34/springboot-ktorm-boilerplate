package com.vgerbot.auth.data

import jakarta.validation.constraints.NotBlank

/**
 * 认证请求 DTO
 * 
 * 支持两种登录方式：
 * 1. 传统方式：直接传明文密码（password字段）
 * 2. 加密方式：传加密密码和密钥ID（encryptedPassword + keyId字段）
 */
data class AuthRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,
    
    /**
     * 明文密码（传统方式）
     */
    val password: String? = null,
    
    /**
     * 加密密码（Base64编码的RSA加密密码）
     */
    val encryptedPassword: String? = null,
    
    /**
     * 密钥ID（用于获取私钥解密）
     */
    val keyId: String? = null
) {
    /**
     * 验证请求是否有效
     * 必须提供 password 或 (encryptedPassword + keyId)
     */
    fun isValid(): Boolean {
        return when {
            !password.isNullOrBlank() -> true
            !encryptedPassword.isNullOrBlank() && !keyId.isNullOrBlank() -> true
            else -> false
        }
    }
    
    /**
     * 是否为加密登录
     */
    fun isEncrypted(): Boolean {
        return !encryptedPassword.isNullOrBlank() && !keyId.isNullOrBlank()
    }
}
