package com.vgerbot.auth

import com.vgerbot.auth.data.AuthRequest
import com.vgerbot.auth.data.RefreshTokenRequest
import com.vgerbot.auth.data.TokenResponse
import com.vgerbot.auth.data.TokenType
import com.vgerbot.auth.exception.InvalidTokenException
import com.vgerbot.auth.exception.JwtAuthenticationException
import com.vgerbot.common.controller.*
import com.vgerbot.common.exception.ConflictException
import com.vgerbot.common.exception.UnauthorizedException
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
@RequestMapping("public/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val jwtTokenUtils: JwtTokenUtils,
    private val userService: UserService,
    private val jwtProperties: JwtProperties
) {
    
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    
    /**
     * User login
     * 
     * @param req Authentication request (username and password)
     * @return Token response
     */
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Login successful"),
        ApiResponse(responseCode = "401", description = "Invalid credentials"),
        ApiResponse(responseCode = "500", description = "Internal server error")
    )
    @PostMapping("login")
    fun login(
        @Parameter(description = "Authentication request", required = true)
        @Valid @RequestBody req: AuthRequest
    ): ResponseEntity<Map<String, Any>> {
        try {
            // 认证用户
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(req.username, req.password)
            )
            
            // 获取用户详情
            val userDetails = authentication.principal as? ExtendedUserDetails
                ?: userDetailsService.loadUserByUsername(req.username) as? ExtendedUserDetails
                ?: throw UnauthorizedException("用户认证失败")
            
            // 生成 Token
            val tokenResponse = generateTokenResponse(userDetails)
            
            logger.info("User logged in successfully: ${req.username}")
            
            return tokenResponse.ok("登录成功")
            
        } catch (e: BadCredentialsException) {
            logger.warn("Login failed for user ${req.username}: Invalid credentials")
            throw UnauthorizedException("用户名或密码错误")
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
    @PostMapping("register")
    fun register(
        @Parameter(description = "User creation data", required = true)
        @Valid @RequestBody userDto: CreateUserDto
    ): ResponseEntity<Map<String, Any>> {
        val success = userService.createUser(userDto)
        
        if (!success) {
            throw ConflictException("用户已存在")
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
    @PostMapping("refresh")
    fun refresh(
        @Parameter(description = "Refresh token request", required = true)
        @Valid @RequestBody req: RefreshTokenRequest
    ): ResponseEntity<Map<String, Any>> {
        val refreshToken = req.refreshToken
        
        // 验证 Token 类型
        val tokenType = jwtTokenUtils.getTokenType(refreshToken)
        if (tokenType != TokenType.REFRESH) {
            logger.warn("Invalid token type for refresh: $tokenType")
            throw UnauthorizedException("无效的令牌类型")
        }
        
        // 获取用户名
        val username = jwtTokenUtils.getUsernameFromToken(refreshToken)
            ?: throw UnauthorizedException("无效的刷新令牌")
        
        // 加载用户信息
        val userDetails = userDetailsService.loadUserByUsername(username) as? ExtendedUserDetails
            ?: throw UnauthorizedException("用户不存在")
        
        // 验证 Refresh Token
        if (!jwtTokenUtils.validateRefreshToken(refreshToken, userDetails)) {
            throw UnauthorizedException("刷新令牌无效或已过期")
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
    @PostMapping("logout")
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
    @GetMapping("me")
    fun getCurrentUser(): ResponseEntity<Map<String, Any>> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw UnauthorizedException("用户未认证")
        
        val userDetails = authentication.principal as? ExtendedUserDetails
            ?: throw UnauthorizedException("用户未认证")
        
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
