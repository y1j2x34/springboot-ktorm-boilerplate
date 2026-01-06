package com.vgerbot.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
class JwtPasswordEncoderConfiguration {

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}
