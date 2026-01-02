package com.vgerbot.common.dto

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * 通用 API 响应结构
 * 使用 sealed class 实现类型安全的响应封装
 */
sealed class ApiResponse<out T> {
    /**
     * 成功响应
     */
    data class Success<T>(
        val data: T,
        val message: String = "操作成功",
        val code: Int = 200
    ) : ApiResponse<T>()

    /**
     * 失败响应
     */
    data class Error(
        val message: String,
        val code: Int,
        val details: Map<String, Any>? = null
    ) : ApiResponse<Nothing>()
}

/**
 * 响应构建器扩展函数
 */
object ApiResponses {
    /**
     * 成功响应 - 带数据
     */
    fun <T> success(data: T, message: String = "操作成功"): ApiResponse.Success<T> {
        return ApiResponse.Success(data, message)
    }

    /**
     * 成功响应 - 仅消息（用于更新、删除等操作）
     */
    fun success(message: String = "操作成功"): ApiResponse.Success<Unit> {
        return ApiResponse.Success(Unit, message)
    }

    /**
     * 错误响应
     */
    fun error(
        message: String,
        code: Int = 400,
        details: Map<String, Any>? = null
    ): ApiResponse.Error {
        return ApiResponse.Error(message, code, details)
    }

    /**
     * 未找到资源
     */
    fun notFound(message: String = "资源不存在"): ApiResponse.Error {
        return ApiResponse.Error(message, 404)
    }

    /**
     * 冲突（如重复创建）
     */
    fun conflict(message: String = "资源冲突"): ApiResponse.Error {
        return ApiResponse.Error(message, 409)
    }

    /**
     * 未授权
     */
    fun unauthorized(message: String = "未授权"): ApiResponse.Error {
        return ApiResponse.Error(message, 401)
    }

    /**
     * 禁止访问
     */
    fun forbidden(message: String = "权限不足"): ApiResponse.Error {
        return ApiResponse.Error(message, 403)
    }

    /**
     * 服务器内部错误
     */
    fun internalError(message: String = "服务器内部错误"): ApiResponse.Error {
        return ApiResponse.Error(message, 500)
    }
}

/**
 * 扩展函数：将 ApiResponse 转换为 ResponseEntity
 */
fun <T> ApiResponse<T>.toResponseEntity(): ResponseEntity<Map<String, Any>> {
    return when (this) {
        is ApiResponse.Success -> {
            val body = mutableMapOf<String, Any>(
                "code" to code,
                "message" to message
            )
            // 只有当 data 不是 Unit 时才添加到响应中
            @Suppress("UNCHECKED_CAST")
            if (data !is Unit) {
                body["data"] = data as Any
            }
            ResponseEntity.status(HttpStatus.valueOf(code)).body(body)
        }
        is ApiResponse.Error -> {
            val body = mutableMapOf<String, Any>(
                "code" to code,
                "message" to message
            )
            details?.let { body.putAll(it) }
            ResponseEntity.status(HttpStatus.valueOf(code)).body(body)
        }
    }
}

