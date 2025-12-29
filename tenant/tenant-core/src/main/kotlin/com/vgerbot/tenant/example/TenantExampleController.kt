package com.vgerbot.com.vgerbot.tenant.example

import com.vgerbot.tenant.com.vgerbot.tenant.security.TenantPrincipal
import com.vgerbot.tenant.com.vgerbot.tenant.utils.TenantUtils
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 租户使用示例控制器
 * 
 * 演示如何在实际业务中使用租户信息
 */
@RestController
@RequestMapping("/api/example")
class TenantExampleController {
    
    /**
     * 示例 1：使用 @AuthenticationPrincipal 获取租户信息
     */
    @GetMapping("/method1")
    fun method1(@AuthenticationPrincipal principal: TenantPrincipal?): Map<String, Any?> {
        if (principal == null) {
            return mapOf("error" to "Not authenticated")
        }
        
        return mapOf(
            "method" to "AuthenticationPrincipal",
            "username" to principal.username,
            "tenantId" to principal.tenantId,
            "tenantCode" to principal.tenantCode,
            "tenantName" to principal.tenantName,
            "hasTenant" to principal.hasTenantInfo()
        )
    }
    
    /**
     * 示例 2：使用 TenantUtils 工具类获取租户信息
     */
    @GetMapping("/method2")
    fun method2(): Map<String, Any?> {
        val tenantInfo = TenantUtils.getCurrentTenant()
        
        return if (tenantInfo != null) {
            mapOf(
                "method" to "TenantUtils",
                "tenantId" to tenantInfo.tenantId,
                "tenantCode" to tenantInfo.tenantCode,
                "tenantName" to tenantInfo.tenantName
            )
        } else {
            mapOf(
                "method" to "TenantUtils",
                "message" to "No tenant context"
            )
        }
    }
    
    /**
     * 示例 3：模拟数据隔离场景
     * 
     * 在实际业务中，你应该在 DAO 层根据租户 ID 过滤数据
     */
    @GetMapping("/products")
    fun getProducts(): Map<String, Any?> {
        val tenantId = TenantUtils.getCurrentTenantId()
        
        if (tenantId == null) {
            return mapOf(
                "error" to "Tenant information is required",
                "message" to "Please ensure user is assigned to a tenant"
            )
        }
        
        // 模拟产品数据（实际应该从数据库查询）
        val products = listOf(
            mapOf(
                "id" to 1,
                "name" to "Product A",
                "tenantId" to tenantId,
                "price" to 99.99
            ),
            mapOf(
                "id" to 2,
                "name" to "Product B",
                "tenantId" to tenantId,
                "price" to 149.99
            )
        )
        
        return mapOf(
            "tenantId" to tenantId,
            "tenantCode" to TenantUtils.getCurrentTenantCode(),
            "products" to products,
            "count" to products.size
        )
    }
    
    /**
     * 示例 4：同时获取用户和租户信息
     */
    @GetMapping("/user-tenant-info")
    fun getUserTenantInfo(@AuthenticationPrincipal principal: TenantPrincipal?): Map<String, Any?> {
        if (principal == null) {
            return mapOf("error" to "Not authenticated")
        }
        
        return mapOf(
            "user" to mapOf(
                "username" to principal.username,
                "accountNonExpired" to principal.isAccountNonExpired,
                "accountNonLocked" to principal.isAccountNonLocked,
                "credentialsNonExpired" to principal.isCredentialsNonExpired,
                "enabled" to principal.isEnabled
            ),
            "tenant" to if (principal.hasTenantInfo()) {
                mapOf(
                    "id" to principal.tenantId,
                    "code" to principal.tenantCode,
                    "name" to principal.tenantName
                )
            } else {
                mapOf("message" to "User has no tenant assigned")
            }
        )
    }
    
    /**
     * 示例 5：检查租户是否存在
     */
    @GetMapping("/check-tenant")
    fun checkTenant(): Map<String, Any?> {
        val hasTenant = TenantUtils.hasTenant()
        val tenantId = TenantUtils.getCurrentTenantId()
        val tenantCode = TenantUtils.getCurrentTenantCode()
        
        return mapOf(
            "hasTenant" to hasTenant,
            "tenantId" to tenantId,
            "tenantCode" to tenantCode,
            "message" to if (hasTenant) {
                "Tenant context is available"
            } else {
                "No tenant context found. User may not be assigned to any tenant."
            }
        )
    }
}

