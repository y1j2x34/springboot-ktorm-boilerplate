package com.vgerbot.dict.exception

import com.vgerbot.dict.validation.DictValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 字典模块全局异常处理器
 */
@RestControllerAdvice
class DictExceptionHandler {
    
    /**
     * 处理字典数据校验异常
     */
    @ExceptionHandler(DictValidationException::class)
    fun handleDictValidationException(e: DictValidationException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf(
                "error" to "VALIDATION_ERROR",
                "message" to e.message!!,
                "dictCode" to e.dictCode,
                "dataValue" to e.dataValue,
                "validationRule" to (e.validationRule ?: "")
            ))
    }
}

