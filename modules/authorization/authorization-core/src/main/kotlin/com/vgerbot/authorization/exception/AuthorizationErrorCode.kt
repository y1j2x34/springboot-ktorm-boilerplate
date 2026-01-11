package com.vgerbot.authorization.exception

import com.vgerbot.common.exception.ErrorCode
import org.springframework.http.HttpStatus

/**
 * 授权模块错误码
 * 模块代码：50
 */
enum class AuthorizationErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : ErrorCode {
    // ==================== 授权通用错误 (5000XX) ====================
    AUTHORIZATION_ERROR(500000, "授权错误", HttpStatus.BAD_REQUEST),
    
    // ==================== 授权参数验证错误 (5001XX) ====================
    AUTHORIZATION_PARAM_INVALID(500100, "授权参数验证失败", HttpStatus.BAD_REQUEST),
    
    // ==================== 授权资源不存在 (5002XX) ====================
    AUTHORIZATION_ROLE_NOT_FOUND(500200, "角色不存在", HttpStatus.NOT_FOUND),
    AUTHORIZATION_PERMISSION_NOT_FOUND(500201, "权限不存在", HttpStatus.NOT_FOUND),
    AUTHORIZATION_USER_ROLE_NOT_FOUND(500202, "用户角色关联不存在", HttpStatus.NOT_FOUND),
    AUTHORIZATION_ROLE_PERMISSION_NOT_FOUND(500203, "角色权限关联不存在", HttpStatus.NOT_FOUND),
    AUTHORIZATION_USER_PERMISSION_NOT_FOUND(500204, "用户权限关联不存在", HttpStatus.NOT_FOUND),
    
    // ==================== 授权资源冲突 (5003XX) ====================
    AUTHORIZATION_ROLE_EXISTS(500300, "角色代码已存在", HttpStatus.CONFLICT),
    AUTHORIZATION_PERMISSION_EXISTS(500301, "权限代码已存在", HttpStatus.CONFLICT),
    AUTHORIZATION_USER_ROLE_EXISTS(500302, "角色已分配给用户", HttpStatus.CONFLICT),
    AUTHORIZATION_ROLE_PERMISSION_EXISTS(500303, "权限已分配给角色", HttpStatus.CONFLICT),
    AUTHORIZATION_USER_PERMISSION_EXISTS(500304, "权限已分配给用户", HttpStatus.CONFLICT),
}

