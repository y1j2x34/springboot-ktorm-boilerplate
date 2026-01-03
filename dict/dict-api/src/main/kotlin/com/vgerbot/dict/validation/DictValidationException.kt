package com.vgerbot.dict.validation

/**
 * 字典数据校验异常
 */
class DictValidationException(
    message: String,
    val dictCode: String,
    val dataValue: String,
    val validationRule: String? = null
) : RuntimeException(message)





