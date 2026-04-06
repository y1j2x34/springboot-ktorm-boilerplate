package com.vgerbot.auth.common.principal

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

/**
 * 兼容 Spring Security 的统一用户主体。
 */
interface AuthenticatedUserDetails : UserDetails, AuthenticatedPrincipal

/**
 * 默认的统一用户主体实现。
 *
 * password 对外部身份用户允许为空，
 * 此时会返回空字符串，避免破坏 UserDetails 接口约束。
 */
data class DefaultAuthenticatedUserDetails(
    override val userId: Int,
    override val principalName: String,
    private val encodedPassword: String? = null,
    override val emailAddress: String? = null,
    override val provider: String? = null,
    override val externalSubject: String? = null,
    override val tenantId: String? = null,
    override val organizationId: String? = null,
    override val authorities: List<String> = emptyList(),
    private val enabled: Boolean = true,
    private val accountNonExpired: Boolean = true,
    private val accountNonLocked: Boolean = true,
    private val credentialsNonExpired: Boolean = true
) : AuthenticatedUserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return authorities.map(::SimpleGrantedAuthority).toMutableList()
    }

    override fun getPassword(): String = encodedPassword ?: ""

    override fun getUsername(): String = principalName

    override fun isAccountNonExpired(): Boolean = accountNonExpired

    override fun isAccountNonLocked(): Boolean = accountNonLocked

    override fun isCredentialsNonExpired(): Boolean = credentialsNonExpired

    override fun isEnabled(): Boolean = enabled
}

fun getCurrentLoginUser(): AuthenticatedUserDetails? {
    val principal = SecurityContextHolder.getContext().authentication.principal
    if (principal is AuthenticatedUserDetails) {
        return principal;
    }
    if (principal.javaClass.simpleName == "org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken") {
        val principalField = principal.javaClass.getDeclaredField("principal");
        principalField.isAccessible = true;
        return principalField.get(principal) as AuthenticatedUserDetails;
    }
    return null
}