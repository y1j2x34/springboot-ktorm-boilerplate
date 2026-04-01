package com.vgerbot.app.configuration

import com.vgerbot.oauth.OAuth2SecurityConfig
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.config.Customizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.DefaultSecurityFilterChain

class CustomLogoutHandler: LogoutSuccessHandler {
    override fun onLogoutSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        request?.session?.invalidate()?.also {
            response?.sendRedirect("/")
        }
    }
}

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class OAuthSecurityConfiguration(
    private val oauth2SecurityConfig: OAuth2SecurityConfig
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): DefaultSecurityFilterChain =
        http
            .cors { it.disable() }
            .csrf { it.disable() }
            .authorizeHttpRequests(Customizer { requests ->
                requests
                    .requestMatchers(
                        "/public/**",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html"
                    ).permitAll()
                    .anyRequest().authenticated()
            })
            .oauth2Login(Customizer { oauth2 ->
                oauth2SecurityConfig.configureOAuth2Login(oauth2)
            })
            .logout(Customizer { logout: LogoutConfigurer<HttpSecurity?>? ->
                logout!!.logoutSuccessHandler(
                    CustomLogoutHandler()
                )
            }).build()
}