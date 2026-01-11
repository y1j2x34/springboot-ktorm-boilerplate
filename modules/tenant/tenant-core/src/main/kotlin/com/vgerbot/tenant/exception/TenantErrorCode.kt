package com.vgerbot.tenant.exception

import com.vgerbot.common.exception.ErrorCode
import org.springframework.http.HttpStatus

/**
 * 租户模块错误码
 * 模块代码：40
 */
enum class TenantErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : ErrorCode {
    // ==================== 租户通用错误 (4000XX) ====================
    TENANT_ERROR(400000, "租户错误", HttpStatus.BAD_REQUEST),
    
    // ==================== 租户参数验证错误 (4001XX) ====================
    TENANT_PARAM_INVALID(400100, "租户参数验证失败", HttpStatus.BAD_REQUEST),
    TENANT_EMAIL_REQUIRED(400101, "邮箱参数不能为空", HttpStatus.BAD_REQUEST),
    
    // ==================== 租户资源不存在 (4002XX) ====================
    TENANT_NOT_FOUND(400200, "租户不存在", HttpStatus.NOT_FOUND),
    TENANT_NOT_FOUND_BY_EMAIL(400201, "未找到匹配的租户", HttpStatus.NOT_FOUND),
    
    // ==================== 租户资源冲突 (4003XX) ====================
    TENANT_EXISTS(400300, "租户已存在", HttpStatus.CONFLICT),
    TENANT_CODE_EXISTS(400301, "租户代码已存在", HttpStatus.CONFLICT),
}

