package com.vgerbot.oauth

import com.vgerbot.auth.JwtTokenUtils
import com.vgerbot.auth.data.TokenResponse
import com.vgerbot.oauth.service.CustomOidcUser
import com.vgerbot.oauth.service.CustomOAuth2User
import com.vgerbot.oauth.service.CustomOAuth2UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * OAuth2 Controller
 * 
 * Handles OAuth2/OIDC authorization code flow callbacks and user information retrieval
 */
@Tag(name = "OAuth2", description = "OAuth2 authentication APIs")
@RestController
@RequestMapping("public/oauth2")
class OAuth2Controller(
    private val jwtTokenUtils: JwtTokenUtils,
    private val customOAuth2UserService: CustomOAuth2UserService
) {
    
    private val logger = LoggerFactory.getLogger(OAuth2Controller::class.java)
    
    /**
     * OAuth2 login success callback endpoint
     * 
     * This endpoint is automatically called by Spring Security OAuth2
     * After successful OAuth2 authentication, generates JWT token and returns it
     */
    @Operation(summary = "OAuth2 login success", description = "Callback endpoint for successful OAuth2 authentication")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OAuth2 login successful"),
        ApiResponse(responseCode = "401", description = "User details not found"),
        ApiResponse(responseCode = "500", description = "OAuth2 login failed")
    )
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
     * OAuth2 login failure callback endpoint
     */
    @Operation(summary = "OAuth2 login failure", description = "Callback endpoint for failed OAuth2 authentication")
    @ApiResponse(responseCode = "401", description = "OAuth2 authentication failed")
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
     * Get current OAuth2 user information
     */
    @Operation(summary = "Get current OAuth2 user", description = "Get information about the currently authenticated OAuth2 user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
        ApiResponse(responseCode = "401", description = "User not authenticated"),
        ApiResponse(responseCode = "500", description = "Failed to get user info")
    )
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

