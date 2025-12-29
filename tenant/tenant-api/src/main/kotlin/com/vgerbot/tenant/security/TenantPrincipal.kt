package com.vgerbot.tenant.com.vgerbot.tenant.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * 包含租户信息的 Principal
 * 
 * 这个类通过装饰器模式包装原有的 UserDetails，添加租户信息
 * 而不需要修改现有的 User 或 jwt-auth 模块
 */
class TenantPrincipal(
    private val delegate: UserDetails,
    val tenantId: Int?,
    val tenantCode: String?,
    val tenantName: String?
) : UserDetails {
    
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return delegate.authorities
    }

    override fun getPassword(): String {
        return delegate.password
    }

    override fun getUsername(): String {
        return delegate.username
    }

    override fun isAccountNonExpired(): Boolean {
        return delegate.isAccountNonExpired
    }

    override fun isAccountNonLocked(): Boolean {
        return delegate.isAccountNonLocked
    }

    override fun isCredentialsNonExpired(): Boolean {
        return delegate.isCredentialsNonExpired
    }

    override fun isEnabled(): Boolean {
        return delegate.isEnabled
    }
    
    /**
     * 获取原始的 UserDetails 对象
     */
    fun getDelegate(): UserDetails {
        return delegate
    }
    
    /**
     * 检查是否有租户信息
     */
    fun hasTenantInfo(): Boolean {
        return tenantId != null
    }
}

