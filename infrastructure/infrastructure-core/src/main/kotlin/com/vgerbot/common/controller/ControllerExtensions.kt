package com.vgerbot.common.controller

import com.vgerbot.common.dto.ApiResponse
import com.vgerbot.common.dto.ApiResponses
import com.vgerbot.common.dto.toResponseEntity
import org.springframework.http.ResponseEntity

/**
 * 控制器扩展函数
 * 简化控制器中的响应构建
 */

/**
 * 成功响应 - 带数据
 */
fun <T> T.ok(message: String = "操作成功"): ResponseEntity<Map<String, Any>> {
    return ApiResponses.success(this, message).toResponseEntity()
}

/**
 * 成功响应 - 仅消息
 */
fun ok(message: String = "操作成功"): ResponseEntity<Map<String, Any>> {
    return ApiResponses.success(message).toResponseEntity()
}

/**
 * 创建成功响应（201）
 */
fun <T> T.created(message: String = "创建成功"): ResponseEntity<Map<String, Any>> {
    return ApiResponse.Success(this, message, 201).toResponseEntity()
}

/**
 * 无内容响应（204）
 */
fun noContent(): ResponseEntity<Map<String, Any>> {
    return ApiResponse.Success(Unit, "操作成功", 204).toResponseEntity()
}

/**
 * 错误响应
 */
fun error(message: String, code: Int = 400): ResponseEntity<Map<String, Any>> {
    return ApiResponses.error(message, code).toResponseEntity()
}

/**
 * 未找到响应
 */
fun notFound(message: String = "资源不存在"): ResponseEntity<Map<String, Any>> {
    return ApiResponses.notFound(message).toResponseEntity()
}

/**
 * 冲突响应
 */
fun conflict(message: String = "资源冲突"): ResponseEntity<Map<String, Any>> {
    return ApiResponses.conflict(message).toResponseEntity()
}

