package com.vgerbot.tenant.com.vgerbot.tenant.utils

import com.vgerbot.tenant.com.vgerbot.tenant.context.TenantContextHolder
import com.vgerbot.tenant.com.vgerbot.tenant.security.TenantAuthenticationToken
import com.vgerbot.tenant.com.vgerbot.tenant.security.TenantPrincipal
import org.springframework.security.core.context.SecurityContextHolder

/**
 * 租户工具类
 * 
 * 提供便捷的方法来获取当前用户的租户信息
 */
object TenantUtils {
    
    /**
     * 从 Spring Security Context 获取租户信息
     */
    fun getTenantFromSecurityContext(): TenantInfo? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        
        // 如果是 TenantAuthenticationToken，直接获取租户信息
        if (authentication is TenantAuthenticationToken) {
            return TenantInfo(
                tenantId = authentication.tenantId,
                tenantCode = authentication.tenantCode,
                tenantName = authentication.tenantName
            )
        }
        
        // 如果 principal 是 TenantPrincipal，从中获取租户信息
        val principal = authentication.principal
        if (principal is TenantPrincipal) {
            return TenantInfo(
                tenantId = principal.tenantId,
                tenantCode = principal.tenantCode,
                tenantName = principal.tenantName
            )
        }
        
        return null
    }
    
    /**
     * 从 TenantContextHolder 获取租户信息
     */
    fun getTenantFromContext(): TenantInfo? {
        val context = TenantContextHolder.getContext() ?: return null
        return TenantInfo(
            tenantId = context.tenantId,
            tenantCode = context.tenantCode,
            tenantName = context.tenantName
        )
    }
    
    /**
     * 获取当前租户信息（优先从 Security Context，其次从 ThreadLocal）
     */
    fun getCurrentTenant(): TenantInfo? {
        return getTenantFromSecurityContext() ?: getTenantFromContext()
    }
    
    /**
     * 获取当前租户 ID
     */
    fun getCurrentTenantId(): Int? {
        return getCurrentTenant()?.tenantId
    }
    
    /**
     * 获取当前租户代码
     */
    fun getCurrentTenantCode(): String? {
        return getCurrentTenant()?.tenantCode
    }
    
    /**
     * 检查是否有租户信息
     */
    fun hasTenant(): Boolean {
        return getCurrentTenant() != null
    }
}

/**
 * 租户信息数据类
 */
data class TenantInfo(
    val tenantId: Int?,
    val tenantCode: String?,
    val tenantName: String?
)

