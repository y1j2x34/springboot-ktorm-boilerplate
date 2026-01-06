package com.vgerbot.auth.data

import jakarta.validation.constraints.NotBlank

/**
 * 认证请求 DTO
 */
data class AuthRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,
    
    @field:NotBlank(message = "Password is required")
    val password: String
)
