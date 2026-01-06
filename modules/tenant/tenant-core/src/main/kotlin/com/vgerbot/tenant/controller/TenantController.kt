package com.vgerbot.tenant.controller

import com.vgerbot.common.controller.*
import com.vgerbot.common.exception.NotFoundException
import com.vgerbot.common.exception.ValidationException
import com.vgerbot.tenant.dto.TenantDto
import com.vgerbot.tenant.service.TenantService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Tenant Controller
 * Provides REST API for tenant management
 */
@Tag(name = "Tenant", description = "Tenant management APIs")
@RestController
@RequestMapping("/tenants")
class TenantController(
    private val tenantService: TenantService
) {
    
    /**
     * Get all tenants
     */
    @Operation(summary = "Get all tenants", description = "Retrieve a list of all tenants")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved tenants")
    @GetMapping
    fun getAllTenants(): ResponseEntity<Map<String, Any>> {
        val tenants = tenantService.getAllTenants()
        return tenants.ok()
    }
    
    /**
     * Get tenant by ID
     */
    @Operation(summary = "Get tenant by ID", description = "Retrieve a tenant by its ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Tenant found"),
        ApiResponse(responseCode = "404", description = "Tenant not found")
    )
    @GetMapping("/{id}")
    fun getTenantById(
        @Parameter(description = "Tenant ID", required = true)
        @PathVariable id: Int
    ): ResponseEntity<Map<String, Any>> {
        val tenant = tenantService.getTenantById(id)
            ?: throw NotFoundException("租户不存在")
        return tenant.ok()
    }
    
    /**
     * Get tenant by code
     */
    @Operation(summary = "Get tenant by code", description = "Retrieve a tenant by its code")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Tenant found"),
        ApiResponse(responseCode = "404", description = "Tenant not found")
    )
    @GetMapping("/code/{code}")
    fun getTenantByCode(
        @Parameter(description = "Tenant code", required = true)
        @PathVariable code: String
    ): ResponseEntity<Map<String, Any>> {
        val tenant = tenantService.getTenantByCode(code)
            ?: throw NotFoundException("租户不存在")
        return tenant.ok()
    }
    
    /**
     * Find tenant by email domain
     */
    @Operation(summary = "Find tenant by email", description = "Find a tenant that matches the email domain")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Tenant found"),
        ApiResponse(responseCode = "400", description = "Invalid email parameter"),
        ApiResponse(responseCode = "404", description = "No matching tenant found")
    )
    @GetMapping("/find-by-email")
    fun findByEmail(
        @Parameter(description = "Email address", required = true)
        @RequestParam email: String
    ): ResponseEntity<Map<String, Any>> {
        if (email.isBlank()) {
            throw ValidationException("邮箱参数不能为空", "email")
        }

        val tenant = tenantService.findTenantByEmail(email)
            ?: throw NotFoundException("未找到匹配的租户")
        return tenant.ok()
    }
    
    /**
     * Get all tenants for a user
     */
    @Operation(summary = "Get tenants for user", description = "Retrieve all tenants associated with a user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user tenants")
    @GetMapping("/user/{userId}")
    fun getTenantsForUser(
        @Parameter(description = "User ID", required = true)
        @PathVariable userId: Int
    ): ResponseEntity<Map<String, Any>> {
        val tenants = tenantService.getTenantsForUser(userId)
        return tenants.ok()
    }
}
