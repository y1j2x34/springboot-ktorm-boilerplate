package com.vgerbot.auth

import com.vgerbot.auth.data.TokenType
import com.vgerbot.auth.exception.InvalidSignatureException
import com.vgerbot.auth.exception.InvalidTokenException
import com.vgerbot.auth.exception.TokenExpiredException
import com.vgerbot.auth.exception.TokenRevokedException
import com.vgerbot.auth.service.TokenBlacklistService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.slf4j.LoggerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT Token 工具类
 * 
 * 提供 Token 的生成、解析、验证功能
 * 支持 Access Token 和 Refresh Token
 */
@Component
class JwtTokenUtils(
    private val properties: JwtProperties,
    private val tokenBlacklistService: TokenBlacklistService
) {
    
    private val logger = LoggerFactory.getLogger(JwtTokenUtils::class.java)
    
    /**
     * 签名密钥（懒加载，避免重复创建）
     */
    private val signingKey: SecretKey by lazy {
        val keyBytes = try {
            Base64.getDecoder().decode(properties.secret)
        } catch (e: IllegalArgumentException) {
            logger.warn("JWT secret is not Base64 encoded, using raw bytes")
            properties.secret.toByteArray(StandardCharsets.UTF_8)
        }
        Keys.hmacShaKeyFor(keyBytes)
    }
    
    companion object {
        // Claims 键名
        const val CLAIM_TOKEN_TYPE = "type"
        const val CLAIM_USER_ID = "uid"
        const val CLAIM_AUTHORITIES = "authorities"
    }
    
    // ==================== Token 生成 ====================
    
    /**
     * 生成 Access Token
     */
    fun generateAccessToken(userDetails: UserDetails, userId: Int? = null): String {
        val claims = buildClaims(userDetails, userId, TokenType.ACCESS)
        return doGenerateToken(claims, userDetails.username, properties.accessTokenExpireTime)
    }
    
    /**
     * 生成 Refresh Token
     */
    fun generateRefreshToken(userDetails: UserDetails, userId: Int? = null): String {
        val claims = buildClaims(userDetails, userId, TokenType.REFRESH)
        return doGenerateToken(claims, userDetails.username, properties.refreshTokenExpireTime)
    }
    
    /**
     * 构建 Claims
     */
    private fun buildClaims(
        userDetails: UserDetails, 
        userId: Int?, 
        tokenType: TokenType
    ): Map<String, Any> {
        val claims = mutableMapOf<String, Any>(
            CLAIM_TOKEN_TYPE to tokenType.name
        )
        
        userId?.let { claims[CLAIM_USER_ID] = it }
        
        val authorities = userDetails.authorities
            .map(GrantedAuthority::getAuthority)
            .filter { it.isNotBlank() }
        
        if (authorities.isNotEmpty()) {
            claims[CLAIM_AUTHORITIES] = authorities
        }
        
        return claims
    }
    
    /**
     * 执行 Token 生成
     */
    private fun doGenerateToken(
        claims: Map<String, Any>, 
        subject: String, 
        expireTimeSeconds: Long
    ): String {
        val now = Date()
        val expiration = Date(now.time + expireTimeSeconds * 1000)
        
        return Jwts.builder()
            .id(UUID.randomUUID().toString()) // jti - 用于黑名单
            .claims(claims)
            .subject(subject)
            .issuer(properties.issuer)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(signingKey)
            .compact()
    }
    
    // ==================== Token 解析 ====================
    
    /**
     * 从 Token 获取用户名
     */
    fun getUsernameFromToken(token: String): String? {
        return try {
            getClaimFromToken(token, Claims::getSubject)
        } catch (e: Exception) {
            logger.debug("Failed to get username from token: ${e.message}")
            null
        }
    }
    
    /**
     * 从 Token 获取过期时间
     */
    fun getExpirationDateFromToken(token: String): Date? {
        return try {
            getClaimFromToken(token, Claims::getExpiration)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 从 Token 获取用户 ID
     */
    fun getUserIdFromToken(token: String): Int? {
        return try {
            getClaimFromToken(token) { claims ->
                claims[CLAIM_USER_ID]?.let { 
                    when (it) {
                        is Int -> it
                        is Number -> it.toInt()
                        is String -> it.toIntOrNull()
                        else -> null
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 从 Token 获取权限列表
     */
    @Suppress("UNCHECKED_CAST")
    fun getAuthoritiesFromToken(token: String): List<String> {
        return try {
            getClaimFromToken(token) { claims ->
                (claims[CLAIM_AUTHORITIES] as? List<String>) ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 获取 Token 类型
     */
    fun getTokenType(token: String): TokenType? {
        return try {
            getClaimFromToken(token) { claims ->
                claims[CLAIM_TOKEN_TYPE]?.toString()?.let { TokenType.valueOf(it) }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取 Token ID (jti)
     */
    fun getTokenId(token: String): String? {
        return try {
            getClaimFromToken(token, Claims::getId)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取指定 Claim
     */
    fun <T> getClaimFromToken(token: String, claimsResolver: (Claims) -> T): T {
        val claims = getAllClaimsFromToken(token)
        return claimsResolver(claims)
    }
    
    /**
     * 解析 Token 获取所有 Claims
     * 
     * @throws InvalidTokenException Token 格式无效
     * @throws TokenExpiredException Token 已过期
     * @throws InvalidSignatureException Token 签名无效
     */
    private fun getAllClaimsFromToken(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            throw TokenExpiredException("Token has expired", e)
        } catch (e: MalformedJwtException) {
            throw InvalidTokenException("Malformed token", e)
        } catch (e: SignatureException) {
            throw InvalidSignatureException("Invalid token signature", e)
        } catch (e: IllegalArgumentException) {
            throw InvalidTokenException("Token is empty or invalid", e)
        } catch (e: Exception) {
            logger.error("Unexpected error parsing token: ${e.message}", e)
            throw InvalidTokenException("Failed to parse token", e)
        }
    }
    
    // ==================== Token 验证 ====================
    
    /**
     * 验证 Access Token
     * 
     * @throws TokenExpiredException Token 已过期
     * @throws InvalidTokenException Token 无效
     * @throws TokenRevokedException Token 已被撤销
     */
    fun validateAccessToken(token: String, userDetails: UserDetails): Boolean {
        return validateToken(token, userDetails, TokenType.ACCESS)
    }
    
    /**
     * 验证 Refresh Token
     */
    fun validateRefreshToken(token: String, userDetails: UserDetails): Boolean {
        return validateToken(token, userDetails, TokenType.REFRESH)
    }
    
    /**
     * 验证 Token（兼容旧接口）
     */
    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        return try {
            validateAccessToken(token, userDetails)
        } catch (e: Exception) {
            logger.debug("Token validation failed: ${e.message}")
            false
        }
    }
    
    /**
     * 执行 Token 验证
     */
    private fun validateToken(
        token: String, 
        userDetails: UserDetails, 
        expectedType: TokenType
    ): Boolean {
        val claims = getAllClaimsFromToken(token)
        
        // 验证用户名
        val username = claims.subject
        if (username != userDetails.username) {
            throw InvalidTokenException("Token username mismatch")
        }
        
        // 验证 Token 类型
        val tokenType = claims[CLAIM_TOKEN_TYPE]?.toString()?.let { TokenType.valueOf(it) }
        if (tokenType != expectedType) {
            throw InvalidTokenException("Invalid token type: expected $expectedType, got $tokenType")
        }
        
        // 检查是否在黑名单中
        val tokenId = claims.id
        if (tokenId != null && tokenBlacklistService.isBlacklisted(tokenId)) {
            throw TokenRevokedException("Token has been revoked")
        }
        
        // 验证过期时间
        val expiration = claims.expiration
        if (expiration != null && expiration.before(Date())) {
            throw TokenExpiredException("Token has expired")
        }
        
        // 验证签发时间合理性（不能是未来时间）
        val issuedAt = claims.issuedAt
        if (issuedAt != null && issuedAt.after(Date(System.currentTimeMillis() + 60000))) {
            throw InvalidTokenException("Token issued in the future")
        }
        
        return true
    }
    
    // ==================== Token 撤销 ====================
    
    /**
     * 撤销 Token（加入黑名单）
     */
    fun revokeToken(token: String) {
        val tokenId = getTokenId(token) ?: return
        val expiration = getExpirationDateFromToken(token) ?: return
        tokenBlacklistService.addToBlacklist(tokenId, expiration)
        logger.info("Token revoked: $tokenId")
    }
    
    /**
     * 检查 Token 是否已被撤销
     */
    fun isTokenRevoked(token: String): Boolean {
        val tokenId = getTokenId(token) ?: return false
        return tokenBlacklistService.isBlacklisted(tokenId)
    }
}
