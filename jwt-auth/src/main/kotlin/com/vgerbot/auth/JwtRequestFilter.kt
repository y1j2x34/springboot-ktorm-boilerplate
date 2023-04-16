package com.vgerbot.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtRequestFilter: OncePerRequestFilter() {
    @Autowired
    lateinit var userDetailsService: CustomUserDetailsService
    @Autowired
    lateinit var jwtTokenUtils: JwtTokenUtils
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        val token = header.split(" ").first().trim();

        val username = jwtTokenUtils.getUsernameFromToken(token);

        val authentication = SecurityContextHolder.getContext().authentication;

        val userDetails =
            (if (authentication != null) userDetailsService.loadUserByUsername(username) else null)
                ?: return;

        if(jwtTokenUtils.validateToken(token, userDetails)) {
            val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities);
            usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken;
        }
        filterChain.doFilter(request, response);
    }
}