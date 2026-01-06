package com.vgerbot.auth

import com.vgerbot.auth.exception.JwtAuthenticationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * JWT 请求过滤器
 * 
 * 拦截请求，验证 JWT Token 并设置 Spring Security 上下文
 */
@Component
class JwtRequestFilter(
    private val userDetailsService: CustomUserDetailsService,
    private val jwtTokenUtils: JwtTokenUtils
) : OncePerRequestFilter() {
    
    private val log = LoggerFactory.getLogger(JwtRequestFilter::class.java)
    
    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractToken(request)
            
            if (token != null && SecurityContextHolder.getContext().authentication == null) {
                authenticateWithToken(token, request)
            }
        } catch (e: JwtAuthenticationException) {
            log.debug("JWT authentication failed: ${e.message}")
            SecurityContextHolder.clearContext()
            // 继续过滤器链，让后续的认证机制或 EntryPoint 处理
        } catch (e: Exception) {
            log.error("Unexpected error during JWT authentication", e)
            SecurityContextHolder.clearContext()
        }
        
        filterChain.doFilter(request, response)
    }
    
    /**
     * 从请求头提取 Token
     */
    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
            return null
        }
        
        return header.substring(BEARER_PREFIX.length).trim()
    }
    
    /**
     * 使用 Token 进行认证
     */
    private fun authenticateWithToken(token: String, request: HttpServletRequest) {
        val username = jwtTokenUtils.getUsernameFromToken(token)
        
        if (username.isNullOrBlank()) {
            log.debug("Could not extract username from token")
            return
        }
        
        val userDetails = userDetailsService.loadUserByUsername(username)
        
        if (userDetails == null) {
            log.debug("User not found: $username")
            return
        }
        
        // 验证 Token（会抛出异常如果验证失败）
        if (jwtTokenUtils.validateToken(token, userDetails)) {
            // 从 Token 中获取权限（如果有）
            val tokenAuthorities = jwtTokenUtils.getAuthoritiesFromToken(token)
                .map { SimpleGrantedAuthority(it) }
            
            // 合并 Token 中的权限和 UserDetails 中的权限
            val authorities = if (tokenAuthorities.isNotEmpty()) {
                tokenAuthorities
            } else {
                userDetails.authorities.toList()
            }
            
            val authentication = UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
            )
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            
            SecurityContextHolder.getContext().authentication = authentication
            
            log.debug("Authentication successful for user: $username")
        }
    }
}
