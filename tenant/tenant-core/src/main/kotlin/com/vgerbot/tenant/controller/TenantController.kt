package com.vgerbot.com.vgerbot.tenant.controller

import com.vgerbot.tenant.com.vgerbot.tenant.security.TenantAuthenticationToken
import com.vgerbot.tenant.com.vgerbot.tenant.security.TenantPrincipal
import com.vgerbot.tenant.com.vgerbot.tenant.service.TenantService
import com.vgerbot.tenant.com.vgerbot.tenant.utils.TenantUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 租户信息控制器
 * 
 * 提供获取当前租户信息的接口，用于演示如何从 Spring Security 上下文中获取租户信息
 */
@RestController
@RequestMapping("/api/tenant")
class TenantController {
    
    @Autowired
    private lateinit var tenantService: TenantService
    
    /**
     * 获取当前用户的租户信息
     */
    @GetMapping("/current")
    fun getCurrentTenant(): Map<String, Any?> {
        val authentication = SecurityContextHolder.getContext().authentication
        
        val result = mutableMapOf<String, Any?>()
        
        // 方式1：从 TenantAuthenticationToken 获取
        if (authentication is TenantAuthenticationToken) {
            result["fromToken"] = mapOf(
                "tenantId" to authentication.tenantId,
                "tenantCode" to authentication.tenantCode,
                "tenantName" to authentication.tenantName
            )
        }
        
        // 方式2：从 TenantPrincipal 获取
        val principal = authentication?.principal
        if (principal is TenantPrincipal) {
            result["fromPrincipal"] = mapOf(
                "tenantId" to principal.tenantId,
                "tenantCode" to principal.tenantCode,
                "tenantName" to principal.tenantName,
                "username" to principal.username
            )
        }
        
        // 方式3：使用 TenantUtils 工具类获取
        val tenantInfo = TenantUtils.getCurrentTenant()
        if (tenantInfo != null) {
            result["fromUtils"] = mapOf(
                "tenantId" to tenantInfo.tenantId,
                "tenantCode" to tenantInfo.tenantCode,
                "tenantName" to tenantInfo.tenantName
            )
        }
        
        return result
    }
    
    /**
     * 获取当前用户的所有租户
     */
    @GetMapping("/my-tenants")
    fun getMyTenants(): Map<String, Any?> {
        val authentication = SecurityContextHolder.getContext().authentication
        authentication?.name ?: return mapOf("error" to "Not authenticated")

        // 这里需要先查询用户 ID（实际使用中可以从 UserService 获取）
        // 为了演示，这里简化处理
        return mapOf(
            "message" to "Use TenantService.getTenantsForUser(userId) to get all tenants for a user"
        )
    }
    
    /**
     * 根据邮箱查找匹配的租户
     * 
     * @param email 邮箱地址
     * @return 匹配的租户列表
     */
    @GetMapping("/find-by-email")
    fun findByEmail(email: String): Map<String, Any?> {
        if (email.isBlank()) {
            return mapOf("error" to "Email parameter is required")
        }

        val tenant = tenantService.findTenantByEmail(email) ?: return mapOf("error" to "Email parameter is required")

        return mapOf(
            "email" to email,
            "matched" to mapOf(
                "id" to tenant.id,
                "code" to tenant.code,
                "name" to tenant.name,
                "description" to tenant.description,
                "emailDomains" to tenant.emailDomains
            )
        )
    }
}

