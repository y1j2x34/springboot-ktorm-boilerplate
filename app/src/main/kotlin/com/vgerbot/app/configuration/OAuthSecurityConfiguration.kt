package com.vgerbot.app.configuration

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import java.util.function.Consumer

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
class CustomLoginSuccessHandler: AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        response?.sendRedirect("/")
    }

}

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class OAuthSecurityConfiguration(
    private val clientRegistrationRepository: ClientRegistrationRepository
) {
    @Bean
    fun idTokenDecoderFactory(): JwtDecoderFactory<ClientRegistration?> {
        val idTokenDecoderFactory = OidcIdTokenDecoderFactory()
        idTokenDecoderFactory.setJwsAlgorithmResolver { SignatureAlgorithm.ES384 }
        return idTokenDecoderFactory
    }


    @Bean
    fun securityFilterChain(http: HttpSecurity): DefaultSecurityFilterChain =
        http
            .cors { it.disable() }
            .csrf { it.disable() }
            .authorizeHttpRequests(Customizer { requests ->
                requests
                    .requestMatchers(
                        "/public/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html"
                    ).permitAll()
                    .anyRequest().authenticated()
            })
            .oauth2Login(Customizer { oauth2: OAuth2LoginConfigurer<HttpSecurity?>? ->
                oauth2!!
                    .authorizationEndpoint(Customizer { authorization ->
                        authorization.authorizationRequestResolver(
                            authorizationRequestResolver(this.clientRegistrationRepository)
                        )
                    })
                    .successHandler(CustomLoginSuccessHandler())
            })
            .logout(Customizer { logout: LogoutConfigurer<HttpSecurity?>? ->
                logout!!.logoutSuccessHandler(
                    CustomLogoutHandler()
                )
            }).build()


    private fun authorizationRequestResolver(
        clientRegistrationRepository: ClientRegistrationRepository
    ): OAuth2AuthorizationRequestResolver {
        val authorizationRequestResolver = DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository, "/oauth2/authorization"
        )
        authorizationRequestResolver.setAuthorizationRequestCustomizer(authorizationRequestCustomizer())

        return authorizationRequestResolver
    }

    private fun authorizationRequestCustomizer(): Consumer<OAuth2AuthorizationRequest.Builder?> {
        return Consumer { customizer: OAuth2AuthorizationRequest.Builder? ->
            customizer!!.additionalParameters(Consumer { params: MutableMap<String?, Any?>? ->
                // Set the prompt parameter to "consent". User will be auto consent if Logto has
                // a valid session
                params!!.put("prompt", "consent")

                // Set the prompt parameter to "login" to force the user to sign in every time
                // params.put("prompt", "login");
                params.put("resource", "http://localhost:3001/api/test")
            })
        }
    }
}