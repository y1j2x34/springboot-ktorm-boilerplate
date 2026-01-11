package com.vgerbot.auth

import com.vgerbot.auth.data.AuthRequest
import com.vgerbot.auth.data.PublicKeyResponse
import com.vgerbot.auth.data.RefreshTokenRequest
import com.vgerbot.auth.data.TokenResponse
import com.vgerbot.auth.data.TokenType
import com.vgerbot.auth.exception.InvalidTokenException
import com.vgerbot.auth.exception.JwtAuthenticationException
import com.vgerbot.auth.exception.KeyExpiredException
import com.vgerbot.auth.exception.KeyNotFoundException
import com.vgerbot.auth.service.RsaKeyService
import com.vgerbot.common.controller.*
import com.vgerbot.auth.exception.AuthErrorCode
import com.vgerbot.common.exception.CommonErrorCode
import com.vgerbot.common.exception.exception
import com.vgerbot.user.dto.CreateUserDto
import com.vgerbot.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

/**
 * Authentication Controller
 * 
 * Provides login, registration, token refresh, logout APIs
 */
@Tag(name = "Authentication", description = "Authentication and authorization APIs")
@RestController
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val jwtTokenUtils: JwtTokenUtils,
    private val userService: UserService,
    private val jwtProperties: JwtProperties,
    private val rsaKeyService: RsaKeyService
) {
    
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    
    /**
     * Get public key for RSA encryption
     * 
     * Generates a temporary RSA key pair for the session.
     * The private key is stored in Redis with 5-minute TTL.
     * 
     * @param sessionId Optional session ID from X-Session-Id header
     * @return Public key information
     */
    @Operation(
        summary = "Get public key",
        description = "Generate a temporary RSA public key for password encryption. Private key is stored in Redis with 5-minute TTL."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Public key generated successfully"),
        ApiResponse(responseCode = "500", description = "Failed to generate key")
    )
    @GetMapping(value = ["public/auth/public-key"])
    fun getPublicKey(
        @Parameter(description = "Session ID from X-Session-Id header")
        @RequestHeader("X-Session-Id", required = false) sessionId: String?
    ): ResponseEntity<Map<String, Any>> {
        try {
            val keyInfo = rsaKeyService.generateKeyPair(sessionId)
            
            val response = PublicKeyResponse(
                keyId = keyInfo.keyId,
                publicKey = keyInfo.publicKey,
                expiresAt = keyInfo.expiresAt,
                algorithm = keyInfo.algorithm
            )
            
            logger.debug("Generated public key for session: {}, keyId: {}", sessionId, keyInfo.keyId)
            
            return response.ok("公钥获取成功")
        } catch (e: Exception) {
            logger.error("Failed to generate public key", e)
            throw RuntimeException("公钥生成失败", e)
        }
    }
    
    /**
     * User login
     * 
     * Supports two login modes:
     * 1. Traditional: plain password (password field)
     * 2. Encrypted: RSA encrypted password (encryptedPassword + keyId fields)
     * 
     * @param req Authentication request
     * @return Token response
     */
    @Operation(
        summary = "User login",
        description = "Authenticate user and return JWT tokens. Supports both plain password and RSA encrypted password."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Login successful"),
        ApiResponse(responseCode = "400", description = "Invalid request format"),
        ApiResponse(responseCode = "401", description = "Invalid credentials or expired key"),
        ApiResponse(responseCode = "500", description = "Internal server error")
    )
    @PostMapping(value = ["public/auth/login"])
    fun login(
        @Parameter(description = "Authentication request", required = true)
        @Valid @RequestBody req: AuthRequest
    ): ResponseEntity<Map<String, Any>> {
        try {
            // 验证请求格式
            if (!req.isValid()) {
                throw AuthErrorCode.AUTH_REQUEST_FORMAT_ERROR.exception()
            }
            
            // 处理密码
            val password = if (req.isEncrypted()) {
                // 加密登录：解密密码
                try {
                    rsaKeyService.decryptPassword(req.keyId!!, req.encryptedPassword!!)
                } catch (e: KeyNotFoundException) {
                    logger.warn("Key not found for login attempt: keyId={}, username={}", req.keyId, req.username)
                    throw AuthErrorCode.AUTH_KEY_NOT_FOUND.exception()
                } catch (e: KeyExpiredException) {
                    logger.warn("Key expired for login attempt: keyId={}, username={}", req.keyId, req.username)
                    throw AuthErrorCode.AUTH_KEY_EXPIRED.exception()
                } catch (e: Exception) {
                    logger.error("Failed to decrypt password for user: ${req.username}", e)
                    throw AuthErrorCode.AUTH_PASSWORD_DECRYPT_FAILED.exception()
                }
            } else {
                // 传统登录：使用明文密码
                req.password ?: throw AuthErrorCode.AUTH_PASSWORD_REQUIRED.exception()
            }
            
            // 认证用户
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(req.username, password)
            )
            
            // 获取用户详情
            val userDetails = authentication.principal as? ExtendedUserDetails
                ?: userDetailsService.loadUserByUsername(req.username) as? ExtendedUserDetails
                ?: throw AuthErrorCode.AUTH_FAILED.exception("用户认证失败")
            
            // 生成 Token
            val tokenResponse = generateTokenResponse(userDetails)
            
            logger.info("User logged in successfully: ${req.username}, encrypted: ${req.isEncrypted()}")
            
            return tokenResponse.ok("登录成功")
            
        } catch (e: BadCredentialsException) {
            logger.warn("Login failed for user ${req.username}: Invalid credentials")
            throw AuthErrorCode.AUTH_INVALID_CREDENTIALS.exception()
        } catch (e: com.vgerbot.common.exception.BusinessException) {
            // 重新抛出，保持错误码
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error during login for user: ${req.username}", e)
            throw AuthErrorCode.AUTH_LOGIN_FAILED.exception()
        }
    }
    
    /**
     * User registration
     * 
     * @param userDto User creation DTO
     * @return Registration result
     */
    @Operation(summary = "User registration", description = "Register a new user account")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "User registered successfully"),
        ApiResponse(responseCode = "409", description = "User already exists"),
        ApiResponse(responseCode = "500", description = "Registration failed")
    )
    @PostMapping("public/auth/register")
    fun register(
        @Parameter(description = "User creation data", required = true)
        @Valid @RequestBody userDto: CreateUserDto
    ): ResponseEntity<Map<String, Any>> {
        val success = userService.createUser(userDto)
        
        if (!success) {
            throw AuthErrorCode.AUTH_USER_EXISTS.exception()
        }
        
        logger.info("User registered successfully: ${userDto.username}")
        return ok("用户注册成功")
    }
    
    /**
     * Refresh token
     * 
     * Use refresh token to get new access token and refresh token
     * 
     * @param req Refresh token request
     * @return New token response
     */
    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
        ApiResponse(responseCode = "500", description = "Token refresh failed")
    )
    @PostMapping("public/auth/refresh")
    fun refresh(
        @Parameter(description = "Refresh token request", required = true)
        @Valid @RequestBody req: RefreshTokenRequest
    ): ResponseEntity<Map<String, Any>> {
        val refreshToken = req.refreshToken
        
        // 验证 Token 类型
        val tokenType = jwtTokenUtils.getTokenType(refreshToken)
        if (tokenType != TokenType.REFRESH) {
            logger.warn("Invalid token type for refresh: $tokenType")
            throw AuthErrorCode.AUTH_REFRESH_TOKEN_TYPE_ERROR.exception()
        }
        
        // 获取用户名
        val username = jwtTokenUtils.getUsernameFromToken(refreshToken)
            ?: throw AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID.exception()
        
        // 加载用户信息
        val userDetails = userDetailsService.loadUserByUsername(username) as? ExtendedUserDetails
            ?: throw AuthErrorCode.AUTH_USER_NOT_FOUND.exception()
        
        // 验证 Refresh Token
        if (!jwtTokenUtils.validateRefreshToken(refreshToken, userDetails)) {
            throw AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID.exception()
        }
        
        // 撤销旧的 Refresh Token
        jwtTokenUtils.revokeToken(refreshToken)
        
        // 生成新的 Token
        val tokenResponse = generateTokenResponse(userDetails)
        
        logger.info("Token refreshed successfully for user: $username")
        
        return tokenResponse.ok("令牌刷新成功")
    }
    
    /**
     * User logout
     * 
     * Revoke current user's tokens
     */
    @Operation(summary = "User logout", description = "Logout user and revoke tokens")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @PostMapping("public/auth/logout")
    fun logout(
        @Parameter(description = "Authorization header with Bearer token")
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<Map<String, Any>> {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            
            // 撤销 Access Token
            jwtTokenUtils.revokeToken(token)
            
            // 清除 Security 上下文
            SecurityContextHolder.clearContext()
            
            logger.info("User logged out successfully")
        }
        
        return ok("登出成功")
    }
    
    /**
     * Get current user information
     */
    @Operation(summary = "Get current user", description = "Get information about the currently authenticated user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
        ApiResponse(responseCode = "401", description = "User not authenticated")
    )
    @GetMapping("public/auth/me")
    fun getCurrentUser(): ResponseEntity<Map<String, Any>> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw CommonErrorCode.COMMON_UNAUTHORIZED.exception()
        
        val userDetails = authentication.principal as? ExtendedUserDetails
            ?: throw CommonErrorCode.COMMON_UNAUTHORIZED.exception()
        
        return mapOf(
            "userId" to userDetails.userId,
            "username" to userDetails.username,
            "authorities" to userDetails.authorities.map { it.authority }
        ).ok()
    }
    
    /**
     * 生成 Token 响应
     */
    private fun generateTokenResponse(userDetails: ExtendedUserDetails): TokenResponse {
        val accessToken = jwtTokenUtils.generateAccessToken(userDetails, userDetails.userId)
        val refreshToken = jwtTokenUtils.generateRefreshToken(userDetails, userDetails.userId)
        
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = jwtProperties.accessTokenExpireTime,
            refreshExpiresIn = jwtProperties.refreshTokenExpireTime
        )
    }
}
