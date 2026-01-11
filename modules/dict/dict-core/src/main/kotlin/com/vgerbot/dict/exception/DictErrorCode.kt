package com.vgerbot.dict.exception

import com.vgerbot.common.exception.ErrorCode
import org.springframework.http.HttpStatus

/**
 * 字典模块错误码
 * 模块代码：60
 */
enum class DictErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : ErrorCode {
    // ==================== 字典通用错误 (6000XX) ====================
    DICT_ERROR(600000, "字典错误", HttpStatus.BAD_REQUEST),
    
    // ==================== 字典参数验证错误 (6001XX) ====================
    DICT_PARAM_INVALID(600100, "字典参数验证失败", HttpStatus.BAD_REQUEST),
    DICT_VALIDATION_ERROR(600101, "字典数据校验失败", HttpStatus.BAD_REQUEST),
    
    // ==================== 字典资源不存在 (6002XX) ====================
    DICT_TYPE_NOT_FOUND(600200, "字典类型不存在", HttpStatus.NOT_FOUND),
    DICT_DATA_NOT_FOUND(600201, "字典数据不存在", HttpStatus.NOT_FOUND),
    DICT_DEFAULT_DATA_NOT_FOUND(600202, "默认字典数据不存在", HttpStatus.NOT_FOUND),
    
    // ==================== 字典资源冲突 (6003XX) ====================
    DICT_TYPE_CODE_EXISTS(600300, "字典类型代码已存在", HttpStatus.CONFLICT),
    DICT_DATA_VALUE_EXISTS(600301, "字典数据值已存在或字典类型不存在", HttpStatus.CONFLICT),
}

