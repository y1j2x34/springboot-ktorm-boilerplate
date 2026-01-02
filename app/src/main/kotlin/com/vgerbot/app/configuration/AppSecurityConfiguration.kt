package com.vgerbot.app.configuration

import com.vgerbot.auth.CustomUserDetailsService
import com.vgerbot.auth.JwtAuthenticationEntryPoint
import com.vgerbot.auth.JwtRequestFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class AppSecurityConfiguration {
    
    @Autowired
    lateinit var jwtRequestFilter: JwtRequestFilter

    @Autowired
    lateinit var userDetailsService: CustomUserDetailsService

    @Autowired
    lateinit var unauthorizedHandler: JwtAuthenticationEntryPoint

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Bean
    fun securityFilterChain(http: HttpSecurity, daoAuthenticationProvider: DaoAuthenticationProvider): DefaultSecurityFilterChain =
        http
            .cors { it.disable() }
            .csrf { it.disable() }
            .exceptionHandling { it.authenticationEntryPoint(unauthorizedHandler) }
            .authorizeHttpRequests { authorize ->
                authorize.requestMatchers(HttpMethod.POST, "/public/**").permitAll()
                authorize.requestMatchers(HttpMethod.GET, "/public/**").permitAll()
                authorize.requestMatchers(HttpMethod.PUT, "/public/**").permitAll()
                authorize.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                authorize.anyRequest().authenticated()
            }
            .authenticationProvider(daoAuthenticationProvider)
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()

    @Bean
    fun authenticationManager(authConfiguration: AuthenticationConfiguration) = 
        authConfiguration.authenticationManager
}
