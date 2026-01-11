package com.vgerbot.captcha.exception

import com.vgerbot.common.exception.ErrorCode
import org.springframework.http.HttpStatus

/**
 * 验证码模块错误码
 * 模块代码：90
 */
enum class CaptchaErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : ErrorCode {
    // ==================== 验证码通用错误 (9000XX) ====================
    CAPTCHA_ERROR(900000, "验证码错误", HttpStatus.BAD_REQUEST),
    
    // ==================== 验证码参数验证错误 (9001XX) ====================
    CAPTCHA_PARAM_INVALID(900100, "验证码参数验证失败", HttpStatus.BAD_REQUEST),
    
    // ==================== 验证码业务逻辑错误 (9006XX) ====================
    CAPTCHA_INVALID(900600, "验证码无效", HttpStatus.BAD_REQUEST),
    CAPTCHA_EXPIRED(900601, "验证码已过期", HttpStatus.BAD_REQUEST),
    CAPTCHA_MISMATCH(900602, "验证码不匹配", HttpStatus.BAD_REQUEST),
}

