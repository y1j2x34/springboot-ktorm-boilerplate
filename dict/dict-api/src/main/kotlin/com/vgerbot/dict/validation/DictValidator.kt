package com.vgerbot.dict.validation

import com.vgerbot.dict.model.DictType

/**
 * 字典数据校验器接口
 */
interface DictValidator {
    /**
     * 校验字典数据值
     * @param dictType 字典类型
     * @param dataValue 待校验的数据值
     * @return 校验结果
     */
    fun validate(dictType: DictType, dataValue: String): ValidationResult
    
    /**
     * 校验字典数据值，校验失败时抛出异常
     * @param dictType 字典类型
     * @param dataValue 待校验的数据值
     * @throws DictValidationException 校验失败时抛出
     */
    fun validateOrThrow(dictType: DictType, dataValue: String)
}

/**
 * 校验结果
 */
data class ValidationResult(
    val isValid: Boolean,
    val message: String? = null
) {
    companion object {
        fun success() = ValidationResult(true)
        fun failure(message: String) = ValidationResult(false, message)
    }
}

