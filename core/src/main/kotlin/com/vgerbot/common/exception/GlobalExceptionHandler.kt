package com.vgerbot.common.exception

import com.vgerbot.common.dto.ApiResponse
import com.vgerbot.common.dto.toResponseEntity
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * 全局异常处理器
 * 统一处理所有异常，返回标准化的 API 响应
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<Map<String, Any>> {
        logger.debug("业务异常: ${e.message}", e)
        val response = ApiResponse.Error(
            message = e.message ?: "业务异常",
            code = e.code,
            details = e.details
        )
        return response.toResponseEntity()
    }

    /**
     * 处理参数验证异常（@Valid 注解）
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        logger.debug("参数验证失败: ${e.message}")
        val errors = e.bindingResult.fieldErrors.associate { error ->
            error.field to (error.defaultMessage ?: "验证失败")
        }
        val response = ApiResponse.Error(
            message = "参数验证失败",
            code = 400,
            details = mapOf("errors" to errors)
        )
        return response.toResponseEntity()
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException::class)
    fun handleBindException(e: BindException): ResponseEntity<Map<String, Any>> {
        logger.debug("绑定异常: ${e.message}")
        val errors = e.fieldErrors.associate { error ->
            error.field to (error.defaultMessage ?: "验证失败")
        }
        val response = ApiResponse.Error(
            message = "参数绑定失败",
            code = 400,
            details = mapOf("errors" to errors)
        )
        return response.toResponseEntity()
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<Map<String, Any>> {
        logger.debug("参数类型不匹配: ${e.message}")
        val response = ApiResponse.Error(
            message = "参数类型错误: ${e.name}",
            code = 400,
            details = mapOf(
                "parameter" to e.name,
                "requiredType" to (e.requiredType?.simpleName ?: "未知")
            )
        )
        return response.toResponseEntity()
    }

    /**
     * 处理请求体不可读异常（JSON 解析错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<Map<String, Any>> {
        logger.debug("请求体解析失败: ${e.message}")
        val response = ApiResponse.Error(
            message = "请求体格式错误",
            code = 400
        )
        return response.toResponseEntity()
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<Map<String, Any>> {
        logger.error("未处理的异常: ${e.message}", e)
        val response = ApiResponse.Error(
            message = "服务器内部错误",
            code = 500
        )
        return response.toResponseEntity()
    }
}

