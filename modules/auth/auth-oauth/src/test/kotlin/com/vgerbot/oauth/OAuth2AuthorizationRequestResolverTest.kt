package com.vgerbot.oauth

import com.fasterxml.jackson.databind.ObjectMapper
import com.vgerbot.oauth.dao.OAuth2ProviderDao
import com.vgerbot.oauth.entity.OAuth2Provider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import java.lang.reflect.Proxy

class OAuth2AuthorizationRequestResolverTest {

    @Test
    fun `should append provider-specific authorization request params`() {
        val clientRegistrationRepository = InMemoryClientRegistrationRepository(
            ClientRegistration.withRegistrationId("logto")
                .clientId("client-id")
                .clientSecret("client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationUri("https://issuer.example.com/oauth2/auth")
                .tokenUri("https://issuer.example.com/oauth2/token")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile")
                .userNameAttributeName("sub")
                .clientName("Logto")
                .build()
        )

        val resolver = DatabaseBackedOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            oauth2ProviderDao(
                OAuth2Provider {
                    registrationId = "logto"
                    authorizationRequestParams = """{"prompt":"consent","resource":"http://localhost:3001/api/test"}"""
                }
            ),
            ObjectMapper()
        )

        val request = MockHttpServletRequest("GET", "/oauth2/authorization/logto")

        val authorizationRequest = resolver.resolve(request, "logto")

        assertEquals("consent", authorizationRequest?.additionalParameters?.get("prompt"))
        assertEquals(
            "http://localhost:3001/api/test",
            authorizationRequest?.additionalParameters?.get("resource")
        )
    }

    private fun oauth2ProviderDao(provider: OAuth2Provider): OAuth2ProviderDao {
        return Proxy.newProxyInstance(
            OAuth2ProviderDao::class.java.classLoader,
            arrayOf(OAuth2ProviderDao::class.java)
        ) { _, method, args ->
            when (method.name) {
                "findByRegistrationId" -> if (args?.firstOrNull() == provider.registrationId) provider else null
                "findAllEnabled" -> emptyList<OAuth2Provider>()
                "toString" -> "TestOAuth2ProviderDao"
                else -> throw UnsupportedOperationException("Method ${method.name} is not implemented in test proxy")
            }
        } as OAuth2ProviderDao
    }
}
