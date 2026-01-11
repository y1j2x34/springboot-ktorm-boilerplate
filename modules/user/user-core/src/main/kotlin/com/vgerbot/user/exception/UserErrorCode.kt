package com.vgerbot.user.exception

import com.vgerbot.common.exception.ErrorCode
import org.springframework.http.HttpStatus

/**
 * 用户模块错误码
 * 模块代码：30
 */
enum class UserErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : ErrorCode {
    // ==================== 用户通用错误 (3000XX) ====================
    USER_ERROR(300000, "用户错误", HttpStatus.BAD_REQUEST),
    
    // ==================== 用户参数验证错误 (3001XX) ====================
    USER_PARAM_INVALID(300100, "用户参数验证失败", HttpStatus.BAD_REQUEST),
    USER_EMAIL_INVALID(300101, "邮箱格式错误", HttpStatus.BAD_REQUEST),
    USER_PHONE_INVALID(300102, "手机号格式错误", HttpStatus.BAD_REQUEST),
    
    // ==================== 用户资源不存在 (3002XX) ====================
    USER_NOT_FOUND(300200, "用户不存在", HttpStatus.NOT_FOUND),
    
    // ==================== 用户资源冲突 (3003XX) ====================
    USER_EXISTS(300300, "用户已存在", HttpStatus.CONFLICT),
    USER_EMAIL_EXISTS(300301, "邮箱已被使用", HttpStatus.CONFLICT),
    USER_PHONE_EXISTS(300302, "手机号已被使用", HttpStatus.CONFLICT),
    USER_USERNAME_EXISTS(300303, "用户名已被使用", HttpStatus.CONFLICT),
}

