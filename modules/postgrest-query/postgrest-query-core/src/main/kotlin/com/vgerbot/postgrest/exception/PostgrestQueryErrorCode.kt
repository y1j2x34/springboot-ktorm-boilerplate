package com.vgerbot.postgrest.exception

import com.vgerbot.common.exception.ErrorCode
import org.springframework.http.HttpStatus

/**
 * PostgREST查询模块错误码
 * 模块代码：80
 */
enum class PostgrestQueryErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : ErrorCode {
    // ==================== PostgREST查询通用错误 (8000XX) ====================
    POSTGREST_QUERY_ERROR(800000, "查询错误", HttpStatus.BAD_REQUEST),
    
    // ==================== PostgREST查询参数验证错误 (8001XX) ====================
    POSTGREST_QUERY_PARAM_INVALID(800100, "查询参数验证失败", HttpStatus.BAD_REQUEST),
    POSTGREST_QUERY_TABLE_NOT_REGISTERED(800101, "表未注册，无法查询", HttpStatus.BAD_REQUEST),
    POSTGREST_QUERY_INSERT_DATA_REQUIRED(800102, "INSERT 操作需要 data 字段", HttpStatus.BAD_REQUEST),
    POSTGREST_QUERY_INSERT_DATA_EMPTY(800103, "INSERT 操作的数据不能为空", HttpStatus.BAD_REQUEST),
    POSTGREST_QUERY_UPDATE_DATA_REQUIRED(800104, "UPDATE 操作需要 data 字段", HttpStatus.BAD_REQUEST),
    POSTGREST_QUERY_UPDATE_WHERE_REQUIRED(800105, "UPDATE 操作必须包含 where 条件", HttpStatus.BAD_REQUEST),
    POSTGREST_QUERY_DELETE_WHERE_REQUIRED(800106, "DELETE 操作必须包含 where 条件", HttpStatus.BAD_REQUEST),
    POSTGREST_QUERY_UPSERT_DATA_REQUIRED(800107, "UPSERT 操作需要 data 字段", HttpStatus.BAD_REQUEST),
    POSTGREST_QUERY_UPSERT_CONFLICT_REQUIRED(800108, "UPSERT 操作需要 onConflict 字段", HttpStatus.BAD_REQUEST),
    POSTGREST_QUERY_UPSERT_CONFLICT_FIELD_NOT_FOUND(800109, "冲突字段不存在", HttpStatus.BAD_REQUEST),
    POSTGREST_QUERY_UPSERT_CONFLICT_FIELD_EMPTY(800110, "冲突字段的值不能为空", HttpStatus.BAD_REQUEST),
    POSTGREST_QUERY_UPSERT_CONFLICT_BUILD_FAILED(800111, "无法构建冲突条件", HttpStatus.BAD_REQUEST),
    POSTGREST_QUERY_COLUMN_NOT_FOUND(800112, "列在表中不存在", HttpStatus.BAD_REQUEST),
    
    // ==================== PostgREST查询权限不足 (8004XX) ====================
    POSTGREST_QUERY_FORBIDDEN(800400, "没有权限执行该操作", HttpStatus.FORBIDDEN),
    
    // ==================== PostgREST查询业务逻辑错误 (8006XX) ====================
    POSTGREST_QUERY_EXECUTION_FAILED(800600, "查询执行失败", HttpStatus.INTERNAL_SERVER_ERROR),
}

