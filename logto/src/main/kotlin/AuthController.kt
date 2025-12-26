package com.vgerbot.logto

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController {

    /**
     * 前端调用此接口获取当前登录用户信息
     * 如果未登录，Spring Security 会返回 401 (基于上面的配置)
     */
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: OidcUser?): Map<String, Any?> {
        if (principal == null) {
            return mapOf("authenticated" to false)
        }
        return mapOf(
            "authenticated" to true,
            "username" to principal.preferredUsername,
            "email" to principal.email,
            "roles" to principal.authorities.map { it.authority }
        )
    }
}