package com.vgerbot.common.exception

import org.springframework.http.HttpStatus

/**
 * 错误码接口
 * 
 * 所有模块的错误码枚举都应实现此接口
 * 
 * 错误码格式：XXYYZZ (6位数字)
 * - XX: 模块代码 (10-99)
 * - YY: 错误类型 (00-99)
 * - ZZ: 具体错误 (00-99)
 * 
 * 模块代码分配：
 * - 10: 通用/基础设施 (Common)
 * - 20: 认证模块 (Auth)
 * - 30: 用户模块 (User)
 * - 40: 租户模块 (Tenant)
 * - 50: 授权模块 (Authorization)
 * - 60: 字典模块 (Dict)
 * - 70: 动态表模块 (DynamicTable)
 * - 80: PostgREST查询模块 (PostgrestQuery)
 * - 90: 验证码模块 (Captcha)
 * 
 * 错误类型：
 * - 00: 通用错误
 * - 01: 参数验证错误
 * - 02: 资源不存在
 * - 03: 资源冲突
 * - 04: 权限不足
 * - 05: 认证失败
 * - 06: 业务逻辑错误
 * - 07: 外部服务错误
 * - 08: 数据格式错误
 * - 09: 操作不允许
 */
interface ErrorCode {
    /**
     * 业务错误码（6位数字）
     */
    val code: Int
    
    /**
     * 错误消息
     */
    val message: String
    
    /**
     * HTTP状态码
     */
    val httpStatus: HttpStatus
        get() = HttpStatus.BAD_REQUEST
}

/**
 * 错误码工具类
 * 提供统一的错误码查找和转换功能
 */
object ErrorCodes {
    /**
     * 通过反射注册模块错误码
     */
    private fun MutableList<ErrorCode>.registerModuleErrorCodes(className: String) {
        try {
            val clazz = Class.forName(className)
            if (clazz.isEnum && ErrorCode::class.java.isAssignableFrom(clazz)) {
                @Suppress("UNCHECKED_CAST")
                val enumValues = clazz.enumConstants as? Array<ErrorCode>
                enumValues?.let { addAll(it) }
            }
        } catch (e: ClassNotFoundException) {
            // 模块未加载时忽略
        } catch (e: Exception) {
            // 其他异常也忽略，避免影响核心功能
        }
    }
    
    /**
     * 所有已注册的错误码枚举值
     * 通过延迟初始化注册所有模块的错误码
     */
    private val allErrorCodes: List<ErrorCode> by lazy {
        mutableListOf<ErrorCode>().apply {
            // 注册通用模块错误码
            addAll(CommonErrorCode.values())
            
            // 注册各模块错误码
            // 注意：由于模块间可能存在编译依赖问题，这里使用反射方式动态加载
            // 如果模块已编译，则注册其错误码；如果未编译或不存在，则忽略
            registerModuleErrorCodes("com.vgerbot.auth.exception.AuthErrorCode")
            registerModuleErrorCodes("com.vgerbot.user.exception.UserErrorCode")
            registerModuleErrorCodes("com.vgerbot.tenant.exception.TenantErrorCode")
            registerModuleErrorCodes("com.vgerbot.authorization.exception.AuthorizationErrorCode")
            registerModuleErrorCodes("com.vgerbot.dict.exception.DictErrorCode")
            registerModuleErrorCodes("com.vgerbot.dynamictable.exception.DynamicTableErrorCode")
            registerModuleErrorCodes("com.vgerbot.postgrest.exception.PostgrestQueryErrorCode")
            registerModuleErrorCodes("com.vgerbot.captcha.exception.CaptchaErrorCode")
        }
    }
    
    /**
     * 根据错误码查找对应的ErrorCode
     */
    fun findByCode(code: Int): ErrorCode? {
        return allErrorCodes.find { it.code == code }
    }
    
    /**
     * 根据HTTP状态码获取默认错误码
     */
    fun getDefaultByHttpStatus(httpStatus: HttpStatus): ErrorCode {
        return CommonErrorCode.values()
            .find { it.httpStatus == httpStatus }
            ?: CommonErrorCode.COMMON_UNKNOWN_ERROR
    }
}
