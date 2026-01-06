package com.vgerbot.auth

import com.vgerbot.user.dto.UserInfoDto
import com.vgerbot.user.service.UserService
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

/**
 * 扩展的 UserDetails，包含用户 ID
 */
interface ExtendedUserDetails : UserDetails {
    val userId: Int
}

/**
 * 自定义 UserDetails 实现
 */
data class CustomUserDetails(
    override val userId: Int,
    private val username: String,
    private val password: String,
    private val email: String,
    private val authorities: List<GrantedAuthority> = emptyList(),
    private val enabled: Boolean = true,
    private val accountNonExpired: Boolean = true,
    private val accountNonLocked: Boolean = true,
    private val credentialsNonExpired: Boolean = true
) : ExtendedUserDetails {
    
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = authorities.toMutableList()
    
    override fun getPassword(): String = password
    
    override fun getUsername(): String = username
    
    override fun isAccountNonExpired(): Boolean = accountNonExpired
    
    override fun isAccountNonLocked(): Boolean = accountNonLocked
    
    override fun isCredentialsNonExpired(): Boolean = credentialsNonExpired
    
    override fun isEnabled(): Boolean = enabled
    
    fun getEmail(): String = email
    
    companion object {
        fun fromUserInfo(userInfo: UserInfoDto, authorities: List<String> = emptyList()): CustomUserDetails {
            return CustomUserDetails(
                userId = userInfo.id,
                username = userInfo.username,
                password = userInfo.password,
                email = userInfo.email,
                authorities = authorities.map { SimpleGrantedAuthority(it) }
            )
        }
    }
}

/**
 * 自定义 UserDetailsService 实现
 * 
 * 从用户服务加载用户信息并转换为 Spring Security 的 UserDetails
 */
@Component
class CustomUserDetailsService(
    private val userService: UserService
) : UserDetailsService {
    
    override fun loadUserByUsername(username: String?): UserDetails? {
        if (username.isNullOrBlank()) {
            return null
        }
        
        val userInfo = userService.findUser(username) ?: return null
        
        // TODO: 从数据库或配置加载用户权限
        // 这里暂时返回空权限列表，可以根据实际需求扩展
        val authorities = loadUserAuthorities(userInfo.id)
        
        return CustomUserDetails.fromUserInfo(userInfo, authorities)
    }
    
    /**
     * 加载用户权限
     * 
     * TODO: 实现从数据库加载用户权限的逻辑
     * 
     * @param userId 用户ID，可用于从数据库查询用户角色/权限
     */
    @Suppress("UNUSED_PARAMETER")
    private fun loadUserAuthorities(userId: Int): List<String> {
        // 示例：默认给所有用户 ROLE_USER 权限
        // TODO: 根据 userId 从数据库查询实际权限
        return listOf("ROLE_USER")
    }
}
