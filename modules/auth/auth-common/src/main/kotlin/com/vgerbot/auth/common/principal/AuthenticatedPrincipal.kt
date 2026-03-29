package com.vgerbot.auth.common.principal

/**
 * 系统统一身份主体抽象。
 *
 * 该接口描述业务层真正关心的用户身份信息，
 * 使后续 JWT、Logto 等认证方式可以映射到统一模型。
 */
interface AuthenticatedPrincipal {
    val userId: Int
    val principalName: String
    val provider: String?
    val externalSubject: String?
    val tenantId: String?
    val organizationId: String?
    val email: String?
    val authorities: List<String>
}
