package com.vgerbot.oauth

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.vgerbot.oauth.dao.OAuth2ProviderDao
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component

@Component
class DatabaseBackedOAuth2AuthorizationRequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository,
    private val oauth2ProviderDao: OAuth2ProviderDao,
    private val objectMapper: ObjectMapper
) : OAuth2AuthorizationRequestResolver {

    private val delegate = DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository,
        AUTHORIZATION_REQUEST_BASE_URI
    )

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val authorizationRequest = delegate.resolve(request) ?: return null
        val registrationId = extractRegistrationId(request) ?: return authorizationRequest
        return customizeAuthorizationRequest(authorizationRequest, registrationId)
    }

    override fun resolve(request: HttpServletRequest, clientRegistrationId: String): OAuth2AuthorizationRequest? {
        val authorizationRequest = delegate.resolve(request, clientRegistrationId) ?: return null
        return customizeAuthorizationRequest(authorizationRequest, clientRegistrationId)
    }

    private fun customizeAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest,
        registrationId: String
    ): OAuth2AuthorizationRequest {
        val provider = oauth2ProviderDao.findByRegistrationId(registrationId) ?: return authorizationRequest
        val rawParams = provider.authorizationRequestParams ?: return authorizationRequest
        val additionalParams = parseAdditionalParameters(rawParams)
        if (additionalParams.isEmpty()) {
            return authorizationRequest
        }

        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .additionalParameters(authorizationRequest.additionalParameters + additionalParams)
            .build()
    }

    private fun parseAdditionalParameters(rawParams: String): Map<String, Any> {
        if (rawParams.isBlank()) {
            return emptyMap()
        }

        return objectMapper.readValue(rawParams, object : TypeReference<Map<String, Any>>() {})
    }

    private fun extractRegistrationId(request: HttpServletRequest): String? {
        val requestUri = request.requestURI ?: return null
        val normalizedBaseUri = "${request.contextPath}$AUTHORIZATION_REQUEST_BASE_URI"
        if (!requestUri.startsWith(normalizedBaseUri)) {
            return null
        }

        return requestUri.removePrefix(normalizedBaseUri).removePrefix("/").ifBlank { null }
    }

    companion object {
        private const val AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/authorization"
    }
}
