package com.vgerbot.dynamictable.exception

import com.vgerbot.common.exception.ErrorCode
import org.springframework.http.HttpStatus

/**
 * 动态表模块错误码
 * 模块代码：70
 */
enum class DynamicTableErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : ErrorCode {
    // ==================== 动态表通用错误 (7000XX) ====================
    DYNAMIC_TABLE_ERROR(700000, "动态表错误", HttpStatus.BAD_REQUEST),
    
    // ==================== 动态表资源不存在 (7002XX) ====================
    DYNAMIC_TABLE_NOT_FOUND(700200, "表未注册", HttpStatus.NOT_FOUND),
}

