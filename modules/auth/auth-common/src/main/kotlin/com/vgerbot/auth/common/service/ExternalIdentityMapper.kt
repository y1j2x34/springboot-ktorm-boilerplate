package com.vgerbot.auth.common.service

import com.vgerbot.auth.common.dto.TokenClaimsDto
import com.vgerbot.user.dto.UserInfoDto

/**
 * 外部身份映射器。
 *
 * 认证模块先将外部 claims 标准化为 TokenClaimsDto，
 * 再通过该接口查找或绑定本地用户。
 */
interface ExternalIdentityMapper {
    /**
     * 根据外部身份查找本地用户。
     */
    fun findByExternalIdentity(provider: String, externalId: String): UserInfoDto?

    /**
     * 将外部身份绑定到已有本地用户。
     */
    fun bindExternalIdentity(userId: Int, claims: TokenClaimsDto): Boolean
}
