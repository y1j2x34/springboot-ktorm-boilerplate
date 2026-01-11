package com.vgerbot.common.exception

import com.vgerbot.common.dto.ApiResponse
import com.vgerbot.common.dto.toResponseEntity
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.LockedException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.async.AsyncRequestTimeoutException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException

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
            businessCode = e.businessCode.takeIf { it != e.code }, // 只有当业务错误码与HTTP状态码不同时才添加
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
     * 处理密码错误异常
     */
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(e: BadCredentialsException): ResponseEntity<Map<String, Any>> {
        logger.debug("认证失败: 密码错误")
        val response = ApiResponse.Error(
            message = "用户名或密码错误",
            code = 401
        )
        return response.toResponseEntity()
    }

    /**
     * 处理用户不存在异常
     */
    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFoundException(e: UsernameNotFoundException): ResponseEntity<Map<String, Any>> {
        logger.debug("认证失败: 用户不存在 - ${e.message}")
        val response = ApiResponse.Error(
            message = "用户名或密码错误",
            code = 401
        )
        return response.toResponseEntity()
    }

    /**
     * 处理账户被禁用异常
     */
    @ExceptionHandler(DisabledException::class)
    fun handleDisabledException(e: DisabledException): ResponseEntity<Map<String, Any>> {
        logger.debug("认证失败: 账户被禁用 - ${e.message}")
        val response = ApiResponse.Error(
            message = "账户已被禁用",
            code = 401
        )
        return response.toResponseEntity()
    }

    /**
     * 处理账户被锁定异常
     */
    @ExceptionHandler(LockedException::class)
    fun handleLockedException(e: LockedException): ResponseEntity<Map<String, Any>> {
        logger.debug("认证失败: 账户被锁定 - ${e.message}")
        val response = ApiResponse.Error(
            message = "账户已被锁定",
            code = 401
        )
        return response.toResponseEntity()
    }

    /**
     * 处理账户已过期异常
     */
    @ExceptionHandler(AccountExpiredException::class)
    fun handleAccountExpiredException(e: AccountExpiredException): ResponseEntity<Map<String, Any>> {
        logger.debug("认证失败: 账户已过期 - ${e.message}")
        val response = ApiResponse.Error(
            message = "账户已过期",
            code = 401
        )
        return response.toResponseEntity()
    }

    /**
     * 处理凭证已过期异常
     */
    @ExceptionHandler(CredentialsExpiredException::class)
    fun handleCredentialsExpiredException(e: CredentialsExpiredException): ResponseEntity<Map<String, Any>> {
        logger.debug("认证失败: 凭证已过期 - ${e.message}")
        val response = ApiResponse.Error(
            message = "凭证已过期，请重新登录",
            code = 401
        )
        return response.toResponseEntity()
    }

    /**
     * 处理其他认证异常（作为兜底处理）
     */
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(e: AuthenticationException): ResponseEntity<Map<String, Any>> {
        logger.debug("认证失败: ${e.message}", e)
        val response = ApiResponse.Error(
            message = "认证失败: ${e.message ?: "未知错误"}",
            code = 401
        )
        return response.toResponseEntity()
    }

    /**
     * 处理权限不足异常（访问被拒绝）
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<Map<String, Any>> {
        logger.debug("权限不足: ${e.message}")
        val response = ApiResponse.Error(
            message = "权限不足，无法访问该资源",
            code = 403
        )
        return response.toResponseEntity()
    }

    /**
     * 处理认证不足异常（如匿名用户访问需要认证的资源）
     */
    @ExceptionHandler(InsufficientAuthenticationException::class)
    fun handleInsufficientAuthenticationException(e: InsufficientAuthenticationException): ResponseEntity<Map<String, Any>> {
        logger.debug("认证不足: ${e.message}")
        val response = ApiResponse.Error(
            message = "请先登录",
            code = 401
        )
        return response.toResponseEntity()
    }

    // ==================== Spring MVC 异常 ====================

    /**
     * 处理缺少必需请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<Map<String, Any>> {
        logger.debug("缺少请求参数: ${e.parameterName}")
        val response = ApiResponse.Error(
            message = "缺少必需的请求参数: ${e.parameterName}",
            code = 400,
            details = mapOf(
                "parameter" to e.parameterName,
                "type" to e.parameterType
            )
        )
        return response.toResponseEntity()
    }

    /**
     * 处理缺少路径变量异常
     */
    @ExceptionHandler(MissingPathVariableException::class)
    fun handleMissingPathVariableException(e: MissingPathVariableException): ResponseEntity<Map<String, Any>> {
        logger.debug("缺少路径变量: ${e.variableName}")
        val response = ApiResponse.Error(
            message = "缺少路径变量: ${e.variableName}",
            code = 400,
            details = mapOf("variable" to e.variableName)
        )
        return response.toResponseEntity()
    }

    /**
     * 处理 Servlet 请求绑定异常
     */
    @ExceptionHandler(ServletRequestBindingException::class)
    fun handleServletRequestBindingException(e: ServletRequestBindingException): ResponseEntity<Map<String, Any>> {
        logger.debug("请求绑定异常: ${e.message}")
        val response = ApiResponse.Error(
            message = "请求参数绑定失败",
            code = 400
        )
        return response.toResponseEntity()
    }

    /**
     * 处理 JSR-303 约束违反异常（@Validated 校验）
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(e: ConstraintViolationException): ResponseEntity<Map<String, Any>> {
        logger.debug("约束校验失败: ${e.message}")
        val errors = e.constraintViolations.associate { violation ->
            violation.propertyPath.toString() to (violation.message ?: "验证失败")
        }
        val response = ApiResponse.Error(
            message = "参数校验失败",
            code = 400,
            details = mapOf("errors" to errors)
        )
        return response.toResponseEntity()
    }

    /**
     * 处理 HTTP 请求方法不支持异常 (405)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<Map<String, Any>> {
        logger.debug("不支持的请求方法: ${e.method}")
        val response = ApiResponse.Error(
            message = "不支持的请求方法: ${e.method}",
            code = 405,
            details = mapOf(
                "method" to e.method,
                "supportedMethods" to (e.supportedMethods?.toList() ?: emptyList<String>())
            )
        )
        return response.toResponseEntity()
    }

    /**
     * 处理不支持的媒体类型异常 (415)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupportedException(e: HttpMediaTypeNotSupportedException): ResponseEntity<Map<String, Any>> {
        logger.debug("不支持的媒体类型: ${e.contentType}")
        val response = ApiResponse.Error(
            message = "不支持的媒体类型: ${e.contentType}",
            code = 415,
            details = mapOf(
                "contentType" to (e.contentType?.toString() ?: "未知"),
                "supportedMediaTypes" to e.supportedMediaTypes.map { it.toString() }
            )
        )
        return response.toResponseEntity()
    }

    /**
     * 处理不可接受的媒体类型异常 (406)
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException::class)
    fun handleHttpMediaTypeNotAcceptableException(e: HttpMediaTypeNotAcceptableException): ResponseEntity<Map<String, Any>> {
        logger.debug("不可接受的媒体类型: ${e.message}")
        val response = ApiResponse.Error(
            message = "无法生成客户端可接受的响应格式",
            code = 406,
            details = mapOf(
                "supportedMediaTypes" to e.supportedMediaTypes.map { it.toString() }
            )
        )
        return response.toResponseEntity()
    }

    /**
     * 处理找不到处理器异常 (404)
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(e: NoHandlerFoundException): ResponseEntity<Map<String, Any>> {
        logger.debug("找不到处理器: ${e.requestURL}")
        val response = ApiResponse.Error(
            message = "请求的资源不存在: ${e.requestURL}",
            code = 404,
            details = mapOf(
                "url" to e.requestURL,
                "method" to e.httpMethod
            )
        )
        return response.toResponseEntity()
    }

    /**
     * 处理找不到资源异常 (404) - Spring Boot 3.x
     */
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(e: NoResourceFoundException): ResponseEntity<Map<String, Any>> {
        logger.debug("找不到资源: ${e.resourcePath}")
        val response = ApiResponse.Error(
            message = "请求的资源不存在",
            code = 404,
            details = mapOf("resourcePath" to e.resourcePath)
        )
        return response.toResponseEntity()
    }

    /**
     * 处理响应消息不可写异常
     */
    @ExceptionHandler(HttpMessageNotWritableException::class)
    fun handleHttpMessageNotWritableException(e: HttpMessageNotWritableException): ResponseEntity<Map<String, Any>> {
        logger.error("响应消息写入失败: ${e.message}", e)
        val response = ApiResponse.Error(
            message = "响应数据序列化失败",
            code = 500
        )
        return response.toResponseEntity()
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceededException(e: MaxUploadSizeExceededException): ResponseEntity<Map<String, Any>> {
        logger.debug("上传文件过大: ${e.message}")
        val response = ApiResponse.Error(
            message = "上传文件大小超过限制",
            code = 413,
            details = mapOf("maxSize" to (e.maxUploadSize))
        )
        return response.toResponseEntity()
    }

    /**
     * 处理异步请求超时异常 (503)
     */
    @ExceptionHandler(AsyncRequestTimeoutException::class)
    fun handleAsyncRequestTimeoutException(e: AsyncRequestTimeoutException): ResponseEntity<Map<String, Any>> {
        logger.warn("异步请求超时: ${e.message}")
        val response = ApiResponse.Error(
            message = "请求处理超时，请稍后重试",
            code = 503
        )
        return response.toResponseEntity()
    }

    // ==================== JWT 异常 ====================

    /**
     * 处理 JWT 过期异常
     */
    @ExceptionHandler(ExpiredJwtException::class)
    fun handleExpiredJwtException(e: ExpiredJwtException): ResponseEntity<Map<String, Any>> {
        logger.debug("JWT 已过期: ${e.message}")
        val response = ApiResponse.Error(
            message = "登录已过期，请重新登录",
            code = 401
        )
        return response.toResponseEntity()
    }

    /**
     * 处理 JWT 格式错误异常
     */
    @ExceptionHandler(MalformedJwtException::class)
    fun handleMalformedJwtException(e: MalformedJwtException): ResponseEntity<Map<String, Any>> {
        logger.debug("JWT 格式错误: ${e.message}")
        val response = ApiResponse.Error(
            message = "无效的认证令牌",
            code = 401
        )
        return response.toResponseEntity()
    }

    /**
     * 处理 JWT 签名验证失败异常
     */
    @ExceptionHandler(SignatureException::class)
    fun handleSignatureException(e: SignatureException): ResponseEntity<Map<String, Any>> {
        logger.debug("JWT 签名验证失败: ${e.message}")
        val response = ApiResponse.Error(
            message = "认证令牌签名无效",
            code = 401
        )
        return response.toResponseEntity()
    }

    /**
     * 处理不支持的 JWT 异常
     */
    @ExceptionHandler(UnsupportedJwtException::class)
    fun handleUnsupportedJwtException(e: UnsupportedJwtException): ResponseEntity<Map<String, Any>> {
        logger.debug("不支持的 JWT: ${e.message}")
        val response = ApiResponse.Error(
            message = "不支持的认证令牌格式",
            code = 401
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

