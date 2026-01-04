package com.vgerbot.oauth

import com.vgerbot.auth.JwtTokenUtils
import com.vgerbot.auth.data.TokenResponse
import com.vgerbot.oauth.service.CustomOidcUser
import com.vgerbot.oauth.service.CustomOAuth2User
import com.vgerbot.oauth.service.CustomOAuth2UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * OAuth2 认证控制器
 * 
 * 处理 OAuth2/OIDC 授权码流程的回调和用户信息获取
 */
@RestController
@RequestMapping("public/oauth2")
class OAuth2Controller(
    private val jwtTokenUtils: JwtTokenUtils,
    private val customOAuth2UserService: CustomOAuth2UserService
) {
    
    private val logger = LoggerFactory.getLogger(OAuth2Controller::class.java)
    
    /**
     * OAuth2 登录成功后的回调端点
     * 
     * 这个端点会被 Spring Security OAuth2 自动调用
     * 在 OAuth2 认证成功后，生成 JWT Token 并返回
     */
    @GetMapping("login/success")
    fun loginSuccess(
        @AuthenticationPrincipal principal: Any?
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val userDetails = extractUserDetails(principal)
            
            if (userDetails == null) {
                logger.warn("OAuth2 login success but user details not found")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    mapOf(
                        "success" to false,
                        "message" to "User details not found"
                    )
                )
            }
            
            // 生成 JWT Token
            val tokenResponse = generateTokenResponse(userDetails)
            
            logger.info("OAuth2 login successful for user: ${userDetails.username}")
            
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "OAuth2 login successful",
                    "data" to tokenResponse
                )
            )
        } catch (e: Exception) {
            logger.error("OAuth2 login success handler failed", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "success" to false,
                    "message" to "OAuth2 login failed: ${e.message}"
                )
            )
        }
    }
    
    /**
     * OAuth2 登录失败的回调端点
     */
    @GetMapping("login/failure")
    fun loginFailure(): ResponseEntity<Map<String, Any>> {
        logger.warn("OAuth2 login failed")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            mapOf(
                "success" to false,
                "message" to "OAuth2 authentication failed"
            )
        )
    }
    
    /**
     * 获取当前 OAuth2 用户信息
     */
    @GetMapping("user")
    fun getCurrentOAuth2User(
        @AuthenticationPrincipal principal: Any?
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            val userDetails = extractUserDetails(principal)
            
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    mapOf(
                        "success" to false,
                        "message" to "User not authenticated"
                    )
                )
            }
            
            val provider = when (principal) {
                is CustomOidcUser -> principal.provider
                is CustomOAuth2User -> principal.provider
                else -> null
            }
            
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "userId" to userDetails.userId,
                    "username" to userDetails.username,
                    "authorities" to userDetails.authorities.map { it.authority },
                    "provider" to provider
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to get OAuth2 user info", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "success" to false,
                    "message" to "Failed to get user info: ${e.message}"
                )
            )
        }
    }
    
    /**
     * 从 Principal 提取 UserDetails
     */
    private fun extractUserDetails(principal: Any?): com.vgerbot.auth.ExtendedUserDetails? {
        return when (principal) {
            is CustomOidcUser -> principal.userDetails
            is CustomOAuth2User -> principal.userDetails
            is OAuth2AuthenticationToken -> {
                val oauth2User = principal.principal
                when (oauth2User) {
                    is CustomOidcUser -> oauth2User.userDetails
                    is CustomOAuth2User -> oauth2User.userDetails
                    else -> null
                }
            }
            else -> null
        }
    }
    
    /**
     * 生成 Token 响应
     */
    private fun generateTokenResponse(userDetails: com.vgerbot.auth.ExtendedUserDetails): TokenResponse {
        val accessToken = jwtTokenUtils.generateAccessToken(userDetails, userDetails.userId)
        val refreshToken = jwtTokenUtils.generateRefreshToken(userDetails, userDetails.userId)
        
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = 3600, // 1 hour
            refreshExpiresIn = 604800 // 7 days
        )
    }
}

