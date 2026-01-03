package com.vgerbot.oauth.service

import com.vgerbot.auth.CustomUserDetails
import com.vgerbot.auth.ExtendedUserDetails
import com.vgerbot.oauth.dao.OAuth2ProviderDao
import com.vgerbot.user.dto.CreateUserDto
import com.vgerbot.user.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

/**
 * 自定义 OAuth2 用户服务
 * 
 * 将 OAuth2/OIDC 用户信息映射到系统的 UserDetails
 * 如果用户不存在，则自动创建用户
 */
@Service
class CustomOAuth2UserService(
    private val userService: UserService,
    private val oauth2ProviderDao: OAuth2ProviderDao
) {
    
    private val logger = LoggerFactory.getLogger(CustomOAuth2UserService::class.java)
    
    /**
     * 处理 OAuth2 用户信息
     */
    fun loadOAuth2User(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oauth2User = delegate.loadUser(userRequest)
        
        val registrationId = userRequest.clientRegistration.registrationId
        val providerConfig = oauth2ProviderDao.findByRegistrationId(registrationId)
            ?: throw OAuth2AuthenticationException("Unknown OAuth2 provider: $registrationId")
        
        // 提取用户信息
        val username = extractUsername(oauth2User, providerConfig.userNameAttributeName)
        val email = extractEmail(oauth2User)
        val name = extractName(oauth2User)
        
        logger.info("OAuth2 user loaded: username=$username, email=$email, provider=$registrationId")
        
        // 查找或创建用户
        val userDetails = findOrCreateUser(username, email, name, registrationId)
        
        // 创建自定义 OAuth2User，包含系统用户信息
        return CustomOAuth2User(oauth2User, userDetails, registrationId)
    }
    
    /**
     * 处理 OIDC 用户信息
     */
    fun loadOidcUser(userRequest: OidcUserRequest): OidcUser {
        val delegate = OidcUserService()
        val oidcUser = delegate.loadUser(userRequest)
        
        val registrationId = userRequest.clientRegistration.registrationId
        val providerConfig = oauth2ProviderDao.findByRegistrationId(registrationId)
            ?: throw OAuth2AuthenticationException("Unknown OAuth2 provider: $registrationId")
        
        // 提取用户信息（优先使用 ID Token 中的信息）
        val username = extractUsernameFromOidc(oidcUser, providerConfig.userNameAttributeName)
        val email = extractEmailFromOidc(oidcUser)
        val name = extractNameFromOidc(oidcUser)
        
        logger.info("OIDC user loaded: username=$username, email=$email, provider=$registrationId")
        
        // 查找或创建用户
        val userDetails = findOrCreateUser(username, email, name, registrationId)
        
        // 创建自定义 OidcUser，包含系统用户信息
        return CustomOidcUser(oidcUser, userDetails, registrationId)
    }
    
    /**
     * 查找或创建用户
     */
    private fun findOrCreateUser(
        username: String,
        email: String?,
        name: String?,
        provider: String
    ): ExtendedUserDetails {
        // 尝试通过用户名查找用户
        var userInfo = userService.findUser(username)
        
        // 如果用户不存在，尝试通过邮箱查找
        if (userInfo == null && email != null) {
            // 注意：这里假设 UserService 有通过邮箱查找的方法
            // 如果没有，可以跳过这一步
        }
        
        // 如果用户不存在，创建新用户
        if (userInfo == null) {
            // OAuth2 用户不需要密码，生成一个随机密码（不会被使用）
            val randomPassword = java.util.UUID.randomUUID().toString()
            
            val createUserDto = CreateUserDto(
                username = username,
                email = email ?: "$username@oauth.local",
                password = randomPassword, // OAuth2 用户不需要密码，但字段是必需的
                phoneNumber = null
            )
            
            val success = userService.createUser(createUserDto)
            if (success) {
                userInfo = userService.findUser(username)
                logger.info("Created new user from OAuth2: $username")
            } else {
                logger.warn("Failed to create user from OAuth2: $username")
                throw OAuth2AuthenticationException("Failed to create user")
            }
        }
        
        if (userInfo == null) {
            throw OAuth2AuthenticationException("User not found and creation failed")
        }
        
        // 转换为 UserDetails
        return CustomUserDetails.fromUserInfo(userInfo, listOf("ROLE_USER"))
    }
    
    /**
     * 从 OAuth2User 提取用户名
     */
    private fun extractUsername(oauth2User: OAuth2User, userNameAttributeName: String): String {
        val username = oauth2User.getAttribute<String>(userNameAttributeName)
            ?: oauth2User.getAttribute<String>("email")
            ?: oauth2User.getAttribute<String>("preferred_username")
            ?: oauth2User.name
        
        return username ?: throw OAuth2AuthenticationException("Cannot extract username from OAuth2 user")
    }
    
    /**
     * 从 OAuth2User 提取邮箱
     */
    private fun extractEmail(oauth2User: OAuth2User): String? {
        return oauth2User.getAttribute<String>("email")
    }
    
    /**
     * 从 OAuth2User 提取姓名
     */
    private fun extractName(oauth2User: OAuth2User): String? {
        return oauth2User.getAttribute<String>("name")
            ?: oauth2User.getAttribute<String>("display_name")
    }
    
    /**
     * 从 OidcUser 提取用户名
     */
    private fun extractUsernameFromOidc(oidcUser: OidcUser, userNameAttributeName: String): String {
        val username = oidcUser.getAttribute<String>(userNameAttributeName)
            ?: oidcUser.email
            ?: oidcUser.preferredUsername
            ?: oidcUser.subject
        
        return username ?: throw OAuth2AuthenticationException("Cannot extract username from OIDC user")
    }
    
    /**
     * 从 OidcUser 提取邮箱
     */
    private fun extractEmailFromOidc(oidcUser: OidcUser): String? {
        return oidcUser.email
    }
    
    /**
     * 从 OidcUser 提取姓名
     */
    private fun extractNameFromOidc(oidcUser: OidcUser): String? {
        return oidcUser.fullName
            ?: oidcUser.name
    }
}

/**
 * 自定义 OAuth2User，包含系统用户信息
 */
class CustomOAuth2User(
    private val delegate: OAuth2User,
    val userDetails: ExtendedUserDetails,
    val provider: String
) : OAuth2User by delegate {
    
    override fun getName(): String {
        return userDetails.username
    }
    
    override fun getAuthorities(): MutableCollection<out org.springframework.security.core.GrantedAuthority> {
        return userDetails.authorities.toMutableList()
    }
}

/**
 * 自定义 OidcUser，包含系统用户信息
 */
class CustomOidcUser(
    private val delegate: OidcUser,
    val userDetails: ExtendedUserDetails,
    val provider: String
) : OidcUser by delegate {
    
    override fun getName(): String {
        return userDetails.username
    }
    
    override fun getAuthorities(): MutableCollection<out org.springframework.security.core.GrantedAuthority> {
        return userDetails.authorities.toMutableList()
    }
}
