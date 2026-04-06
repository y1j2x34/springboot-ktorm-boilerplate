package com.vgerbot.auth.common.service

import com.vgerbot.auth.common.dto.TokenClaimsDto
import com.vgerbot.auth.common.principal.AuthenticatedUserDetails
import com.vgerbot.auth.common.principal.DefaultAuthenticatedUserDetails
import com.vgerbot.user.dto.UserInfoDto
import org.springframework.stereotype.Component

/**
 * 统一用户主体工厂。
 */
interface PrincipalFactory {
    /**
     * 基于本地用户信息与认证声明构建统一主体。
     */
    fun create(
        userInfo: UserInfoDto,
        claims: TokenClaimsDto? = null,
        authorities: List<String> = claims?.authorities ?: emptyList()
    ): AuthenticatedUserDetails
}

/**
 * 默认的统一用户主体工厂实现。
 */
@Component
class DefaultPrincipalFactory : PrincipalFactory {
    override fun create(
        userInfo: UserInfoDto,
        claims: TokenClaimsDto?,
        authorities: List<String>
    ): AuthenticatedUserDetails {
        return DefaultAuthenticatedUserDetails(
            userId = userInfo.id,
            principalName = userInfo.username,
            encodedPassword = userInfo.password,
            emailAddress = userInfo.email,
            provider = claims?.provider ?: userInfo.authProvider,
            externalSubject = claims?.subject ?: userInfo.externalId,
            tenantId = claims?.tenantId,
            organizationId = claims?.organizationId,
            authorities = authorities
        )
    }
}
