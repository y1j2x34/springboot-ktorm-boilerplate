package com.vgerbot.tenant.com.vgerbot.tenant.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/**
 * 包含租户信息的 Authentication Token
 * 
 * 继承自 UsernamePasswordAuthenticationToken，添加租户信息
 * 可以无缝集成到 Spring Security 框架中
 */
class TenantAuthenticationToken : UsernamePasswordAuthenticationToken {
    
    val tenantId: Int?
    val tenantCode: String?
    val tenantName: String?
    
    /**
     * 构造已认证的 Token（包含 authorities）
     */
    constructor(
        principal: Any,
        credentials: Any?,
        authorities: Collection<GrantedAuthority>,
        tenantId: Int?,
        tenantCode: String?,
        tenantName: String?
    ) : super(principal, credentials, authorities) {
        this.tenantId = tenantId
        this.tenantCode = tenantCode
        this.tenantName = tenantName
    }
    
    /**
     * 从 TenantPrincipal 创建已认证的 Token
     */
    constructor(
        tenantPrincipal: TenantPrincipal,
        credentials: Any?,
        authorities: Collection<GrantedAuthority>
    ) : super(tenantPrincipal, credentials, authorities) {
        this.tenantId = tenantPrincipal.tenantId
        this.tenantCode = tenantPrincipal.tenantCode
        this.tenantName = tenantPrincipal.tenantName
    }
    
    /**
     * 从已有的 UsernamePasswordAuthenticationToken 和租户信息创建新 Token
     */
    companion object {
        fun from(
            original: UsernamePasswordAuthenticationToken,
            tenantId: Int?,
            tenantCode: String?,
            tenantName: String?
        ): TenantAuthenticationToken {
            return TenantAuthenticationToken(
                original.principal,
                original.credentials,
                original.authorities,
                tenantId,
                tenantCode,
                tenantName
            )
        }
    }
}

