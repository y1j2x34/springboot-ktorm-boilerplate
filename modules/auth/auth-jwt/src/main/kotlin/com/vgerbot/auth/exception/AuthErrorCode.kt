package com.vgerbot.auth.exception

import com.vgerbot.common.exception.ErrorCode
import com.vgerbot.common.exception.UnauthorizedException
import org.springframework.http.HttpStatus

/**
 * 认证模块错误码
 * 模块代码：20
 */
enum class AuthErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : ErrorCode {
    // ==================== 认证通用错误 (2000XX) ====================
    AUTH_ERROR(200000, "认证错误", HttpStatus.UNAUTHORIZED),
    
    // ==================== 认证参数验证错误 (2001XX) ====================
    AUTH_PARAM_INVALID(200100, "认证参数验证失败", HttpStatus.BAD_REQUEST),
    AUTH_PASSWORD_REQUIRED(200101, "密码不能为空", HttpStatus.BAD_REQUEST),
    AUTH_USERNAME_REQUIRED(200102, "用户名不能为空", HttpStatus.BAD_REQUEST),
    AUTH_REQUEST_FORMAT_ERROR(200103, "请求格式错误：必须提供 password 或 (encryptedPassword + keyId)", HttpStatus.BAD_REQUEST),
    
    // ==================== 认证资源不存在 (2002XX) ====================
    AUTH_USER_NOT_FOUND(200200, "用户不存在", HttpStatus.NOT_FOUND),
    AUTH_CONFIG_NOT_FOUND(200201, "认证配置不存在", HttpStatus.NOT_FOUND),
    
    // ==================== 认证资源冲突 (2003XX) ====================
    AUTH_USER_EXISTS(200300, "用户已存在", HttpStatus.CONFLICT),
    AUTH_CONFIG_EXISTS(200301, "认证配置已存在", HttpStatus.CONFLICT),
    
    // ==================== 认证权限不足 (2004XX) ====================
    AUTH_FORBIDDEN(200400, "认证权限不足", HttpStatus.FORBIDDEN),
    
    // ==================== 认证失败 (2005XX) ====================
    AUTH_FAILED(200500, "认证失败", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_CREDENTIALS(200501, "用户名或密码错误", HttpStatus.UNAUTHORIZED),
    AUTH_ACCOUNT_DISABLED(200502, "账户已被禁用", HttpStatus.UNAUTHORIZED),
    AUTH_ACCOUNT_LOCKED(200503, "账户已被锁定", HttpStatus.UNAUTHORIZED),
    AUTH_ACCOUNT_EXPIRED(200504, "账户已过期", HttpStatus.UNAUTHORIZED),
    AUTH_CREDENTIALS_EXPIRED(200505, "凭证已过期，请重新登录", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_INVALID(200506, "无效的认证令牌", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_EXPIRED(200507, "登录已过期，请重新登录", HttpStatus.UNAUTHORIZED),
    AUTH_REFRESH_TOKEN_INVALID(200508, "刷新令牌无效或已过期", HttpStatus.UNAUTHORIZED),
    AUTH_REFRESH_TOKEN_TYPE_ERROR(200509, "无效的令牌类型", HttpStatus.UNAUTHORIZED),
    AUTH_KEY_NOT_FOUND(200510, "安全验证失败，请刷新页面重试", HttpStatus.UNAUTHORIZED),
    AUTH_KEY_EXPIRED(200511, "安全会话已过期，请重新获取公钥", HttpStatus.UNAUTHORIZED),
    AUTH_PASSWORD_DECRYPT_FAILED(200512, "密码解密失败", HttpStatus.UNAUTHORIZED),
    AUTH_JWT_MALFORMED(200513, "无效的认证令牌", HttpStatus.UNAUTHORIZED),
    AUTH_JWT_SIGNATURE_INVALID(200514, "认证令牌签名无效", HttpStatus.UNAUTHORIZED),
    AUTH_JWT_UNSUPPORTED(200515, "不支持的认证令牌格式", HttpStatus.UNAUTHORIZED),
    
    // ==================== 认证业务逻辑错误 (2006XX) ====================
    AUTH_BUSINESS_ERROR(200600, "认证业务逻辑错误", HttpStatus.BAD_REQUEST),
    AUTH_LOGIN_FAILED(200601, "登录失败，请稍后重试", HttpStatus.BAD_REQUEST),
    
    // ==================== 认证外部服务错误 (2007XX) ====================
    AUTH_EXTERNAL_ERROR(200700, "外部认证服务错误", HttpStatus.BAD_GATEWAY),
    AUTH_WECHAT_ERROR(200701, "微信认证服务错误", HttpStatus.BAD_GATEWAY),
    AUTH_OAUTH_ERROR(200702, "OAuth认证服务错误", HttpStatus.BAD_GATEWAY),
    
    // ==================== 认证数据格式错误 (2008XX) ====================
    AUTH_DATA_FORMAT_ERROR(200800, "认证数据格式错误", HttpStatus.BAD_REQUEST),
    AUTH_LOGIN_TYPE_INVALID(200801, "无效的登录类型", HttpStatus.BAD_REQUEST),
}