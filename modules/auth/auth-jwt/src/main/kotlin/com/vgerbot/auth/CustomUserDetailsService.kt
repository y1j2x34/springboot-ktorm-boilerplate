package com.vgerbot.auth

import com.vgerbot.auth.common.service.PrincipalFactory
import com.vgerbot.user.service.UserService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

/**
 * 自定义 UserDetailsService 实现
 * 
 * 从用户服务加载用户信息并转换为 Spring Security 的 UserDetails
 */
@Component
class CustomUserDetailsService(
    private val userService: UserService,
    private val principalFactory: PrincipalFactory
) : UserDetailsService {
    
    override fun loadUserByUsername(username: String?): UserDetails? {
        if (username.isNullOrBlank()) {
            return null
        }
        
        val userInfo = userService.findUser(username) ?: return null
        
        // TODO: 从数据库或配置加载用户权限
        // 这里暂时返回空权限列表，可以根据实际需求扩展
        val authorities = loadUserAuthorities(userInfo.id)
        
        return principalFactory.create(userInfo, null, authorities)
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
        // 示例：默认给所有用户 user 权限
        // TODO: 根据 userId 从数据库查询实际权限
        return listOf("user")
    }
}
