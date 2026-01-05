package com.vgerbot.auth

import com.vgerbot.auth.data.AuthRequest
import com.vgerbot.auth.data.RefreshTokenRequest
import com.vgerbot.auth.data.TokenResponse
import com.vgerbot.auth.data.TokenType
import com.vgerbot.auth.exception.InvalidTokenException
import com.vgerbot.auth.exception.JwtAuthenticationException
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
import org.springframework.http.HttpStatus
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
    ): ResponseEntity<TokenResponse> {
        try {
            // 认证用户
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(req.username, req.password)
            )
            
            // 获取用户详情
            val userDetails = authentication.principal as? ExtendedUserDetails
                ?: userDetailsService.loadUserByUsername(req.username) as? ExtendedUserDetails
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            
            // 生成 Token
            val tokenResponse = generateTokenResponse(userDetails)
            
            logger.info("User logged in successfully: ${req.username}")
            
            return ResponseEntity.ok(tokenResponse)
            
        } catch (e: BadCredentialsException) {
            logger.warn("Login failed for user ${req.username}: Invalid credentials")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } catch (e: Exception) {
            logger.error("Login failed for user ${req.username}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
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
        return try {
            val success = userService.createUser(userDto)
            
            if (success) {
                logger.info("User registered successfully: ${userDto.username}")
                ResponseEntity.status(HttpStatus.CREATED).body(
                    mapOf(
                        "success" to true,
                        "message" to "User registered successfully"
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.CONFLICT).body(
                    mapOf(
                        "success" to false,
                        "message" to "User already exists"
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Registration failed for user ${userDto.username}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                mapOf(
                    "success" to false,
                    "message" to "Registration failed"
                )
            )
        }
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
    ): ResponseEntity<TokenResponse> {
        return try {
            val refreshToken = req.refreshToken
            
            // 验证 Token 类型
            val tokenType = jwtTokenUtils.getTokenType(refreshToken)
            if (tokenType != TokenType.REFRESH) {
                logger.warn("Invalid token type for refresh: $tokenType")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            }
            
            // 获取用户名
            val username = jwtTokenUtils.getUsernameFromToken(refreshToken)
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            
            // 加载用户信息
            val userDetails = userDetailsService.loadUserByUsername(username) as? ExtendedUserDetails
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            
            // 验证 Refresh Token
            if (!jwtTokenUtils.validateRefreshToken(refreshToken, userDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            }
            
            // 撤销旧的 Refresh Token
            jwtTokenUtils.revokeToken(refreshToken)
            
            // 生成新的 Token
            val tokenResponse = generateTokenResponse(userDetails)
            
            logger.info("Token refreshed successfully for user: $username")
            
            ResponseEntity.ok(tokenResponse)
            
        } catch (e: JwtAuthenticationException) {
            logger.warn("Token refresh failed: ${e.message}")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } catch (e: Exception) {
            logger.error("Token refresh failed", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
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
        return try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(mapOf("success" to true, "message" to "Already logged out"))
            }
            
            val token = authHeader.substring(7)
            
            // 撤销 Access Token
            jwtTokenUtils.revokeToken(token)
            
            // 清除 Security 上下文
            SecurityContextHolder.clearContext()
            
            logger.info("User logged out successfully")
            
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Logged out successfully"
                )
            )
        } catch (e: Exception) {
            logger.error("Logout failed", e)
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Logged out"
                )
            )
        }
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
    fun getCurrentUser(): ResponseEntity<Map<String, Any?>> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        
        val userDetails = authentication.principal as? ExtendedUserDetails
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        
        return ResponseEntity.ok(
            mapOf(
                "userId" to userDetails.userId,
                "username" to userDetails.username,
                "authorities" to userDetails.authorities.map { it.authority }
            )
        )
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
