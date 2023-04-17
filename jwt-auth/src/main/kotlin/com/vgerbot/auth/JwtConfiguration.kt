package com.vgerbot.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class JwtConfiguration {
    @Autowired
    lateinit var jwtRequestFilter: JwtRequestFilter;

    @Autowired
    lateinit var userDetailsService: CustomUserDetailsService

    @Autowired
    lateinit var unauthorizedHandler: JwtAuthenticationEntryPoint

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder;

    @Bean
    fun configure(http: HttpSecurity): DefaultSecurityFilterChain? =
        http.run {
            cors().and().csrf().disable()
            exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
            authorizeRequests {
                // public endpoints
                it.antMatchers(HttpMethod.POST, "/public/**").permitAll()
                it.antMatchers(HttpMethod.GET, "/public/**").permitAll()
                it.antMatchers(HttpMethod.PUT, "/public/**").permitAll()
                // private endpoint
                it.anyRequest().authenticated()
            }
            authenticationProvider(createAuthenticationProvider())
            addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
        }.build()


    @Bean
    fun createAuthenticationProvider(): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    fun authenticationManager(authConfiguration: AuthenticationConfiguration) = authConfiguration.authenticationManager;
}