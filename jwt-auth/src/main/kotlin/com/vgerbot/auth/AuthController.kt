package com.vgerbot.auth

import com.vgerbot.auth.data.AuthRequest
import com.vgerbot.auth.data.RefreshTokenRequest
import com.vgerbot.auth.data.TokenResponse
import com.vgerbot.auth.data.TokenType
import com.vgerbot.auth.exception.InvalidTokenException
import com.vgerbot.auth.exception.JwtAuthenticationException
import com.vgerbot.user.dto.CreateUserDto
import com.vgerbot.user.service.UserService
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
 * 认证控制器
 * 
 * 提供登录、注册、Token 刷新、登出等接口
 */
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
     * 用户登录
     * 
     * @param req 认证请求（用户名和密码）
     * @return Token 响应
     */
    @PostMapping("login")
    fun login(@Valid @RequestBody req: AuthRequest): ResponseEntity<TokenResponse> {
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
     * 用户注册
     * 
     * @param userDto 用户创建 DTO
     * @return 注册结果
     */
    @PostMapping("register")
    fun register(@Valid @RequestBody userDto: CreateUserDto): ResponseEntity<Map<String, Any>> {
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
     * 刷新 Token
     * 
     * 使用 Refresh Token 获取新的 Access Token 和 Refresh Token
     * 
     * @param req Refresh Token 请求
     * @return 新的 Token 响应
     */
    @PostMapping("refresh")
    fun refresh(@Valid @RequestBody req: RefreshTokenRequest): ResponseEntity<TokenResponse> {
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
     * 登出
     * 
     * 撤销当前用户的 Token
     */
    @PostMapping("logout")
    fun logout(@RequestHeader("Authorization") authHeader: String?): ResponseEntity<Map<String, Any>> {
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
     * 获取当前用户信息
     */
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
