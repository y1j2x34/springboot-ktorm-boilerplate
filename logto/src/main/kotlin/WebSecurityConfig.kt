package com.vgerbot.logto

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import org.springframework.security.web.DefaultSecurityFilterChain



@Configuration
@EnableWebSecurity
class WebSecurityConfig {

    @Bean
    fun idTokenDecoderFactory(): JwtDecoderFactory<ClientRegistration?> {
        val idTokenDecoderFactory: OidcIdTokenDecoderFactory = OidcIdTokenDecoderFactory()
        idTokenDecoderFactory.setJwsAlgorithmResolver({ clientRegistration -> SignatureAlgorithm.ES384 })
        return idTokenDecoderFactory
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): DefaultSecurityFilterChain =
        http.authorizeRequests { authorizeRequests ->
            authorizeRequests
                .antMatchers(HttpMethod.GET, "/", "/sign-in", "/check", "/error").permitAll()
                .anyRequest().authenticated()
        }
            .oauth2Login { oauth2Login ->
//                oauth2Login.loginPage("/sign-in")
                oauth2Login.successHandler { _, response, _ ->
                    response?.sendRedirect("/")
                }
            }
            .logout { logout ->
                logout.invalidateHttpSession(false)
                    .logoutSuccessUrl("/")
            }
            .build()
}