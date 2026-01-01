package com.vgerbot.dict.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vgerbot.dict.entity.DictType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 字典数据校验器实现
 */
@Component
class DictValidatorImpl : DictValidator {
    
    private val logger = LoggerFactory.getLogger(DictValidatorImpl::class.java)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    
    override fun validate(dictType: DictType, dataValue: String): ValidationResult {
        // 如果没有配置校验规则，直接通过
        val ruleJson = dictType.validationRule
        if (ruleJson.isNullOrBlank()) {
            return ValidationResult.success()
        }
        
        return try {
            // 解析校验规则
            val rule = parseValidationRule(ruleJson)
            
            // 执行校验
            if (rule.validate(dataValue)) {
                ValidationResult.success()
            } else {
                // 使用自定义消息或默认消息
                val message = dictType.validationMessage?.takeIf { it.isNotBlank() } 
                    ?: rule.getDefaultMessage()
                ValidationResult.failure(message)
            }
        } catch (e: Exception) {
            logger.error("Failed to validate dict data: dictCode=${dictType.dictCode}, value=$dataValue", e)
            ValidationResult.failure("校验规则解析失败: ${e.message}")
        }
    }
    
    override fun validateOrThrow(dictType: DictType, dataValue: String) {
        val result = validate(dictType, dataValue)
        if (!result.isValid) {
            throw DictValidationException(
                message = result.message ?: "数据校验失败",
                dictCode = dictType.dictCode,
                dataValue = dataValue,
                validationRule = dictType.validationRule
            )
        }
    }
    
    /**
     * 解析校验规则 JSON
     */
    private fun parseValidationRule(ruleJson: String): ValidationRule {
        return try {
            objectMapper.readValue<ValidationRule>(ruleJson)
        } catch (e: Exception) {
            logger.error("Failed to parse validation rule: $ruleJson", e)
            throw IllegalArgumentException("无效的校验规则格式: ${e.message}")
        }
    }
}

