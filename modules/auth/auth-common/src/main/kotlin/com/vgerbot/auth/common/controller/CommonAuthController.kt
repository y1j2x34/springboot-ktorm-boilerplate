package com.vgerbot.auth.common.controller

import com.vgerbot.auth.common.principal.AuthenticatedUserDetails
import com.vgerbot.common.controller.ok
import com.vgerbot.common.exception.CommonErrorCode
import com.vgerbot.common.exception.exception
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Common Authentication Controller
 * 
 * Provides unified authentication related APIs
 */
@Tag(name = "Authentication (Common)", description = "Unified authentication and authorization APIs")
@RestController
@RequestMapping("public/auth")
class CommonAuthController {

    /**
     * Get current user information
     */
    @Operation(summary = "Get current user", description = "Get information about the currently authenticated user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
        ApiResponse(responseCode = "401", description = "User not authenticated")
    )
    @GetMapping("me")
    fun getCurrentUser(): ResponseEntity<Map<String, Any>> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw CommonErrorCode.COMMON_UNAUTHORIZED.exception()
        
        val userDetails = authentication.principal as? AuthenticatedUserDetails
            ?: throw CommonErrorCode.COMMON_UNAUTHORIZED.exception()
        
        return mapOf(
            "userId" to userDetails.userId,
            "username" to userDetails.username,
            "authorities" to userDetails.authorities,
            "provider" to userDetails.provider,
            "tenantId" to userDetails.tenantId,
            "organizationId" to userDetails.organizationId,
            "email" to userDetails.email
        ).ok()
    }
}
