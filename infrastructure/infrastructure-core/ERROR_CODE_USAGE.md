# 错误码体系使用指南

## 概述

本项目采用模块化的错误码设计，每个模块拥有独立的错误码枚举，通过接口统一管理。这种设计降低了模块间的耦合性，提高了代码的可维护性和可扩展性。

## 架构设计

### 1. ErrorCode 接口

所有错误码枚举都实现 `ErrorCode` 接口：

```kotlin
interface ErrorCode {
    val code: Int          // 业务错误码（6位数字）
    val message: String     // 错误消息
    val httpStatus: HttpStatus  // HTTP状态码
}
```

### 2. 错误码格式

错误码格式：`XXYYZZ` (6位数字)
- **XX**: 模块代码 (10-99)
- **YY**: 错误类型 (00-99)
- **ZZ**: 具体错误 (00-99)

### 3. 模块代码分配

- `10`: 通用/基础设施 (Common)
- `20`: 认证模块 (Auth)
- `30`: 用户模块 (User)
- `40`: 租户模块 (Tenant)
- `50`: 授权模块 (Authorization)
- `60`: 字典模块 (Dict)
- `70`: 动态表模块 (DynamicTable)
- `80`: PostgREST查询模块 (PostgrestQuery)
- `90`: 验证码模块 (Captcha)

### 4. 错误类型

- `00`: 通用错误
- `01`: 参数验证错误
- `02`: 资源不存在
- `03`: 资源冲突
- `04`: 权限不足
- `05`: 认证失败
- `06`: 业务逻辑错误
- `07`: 外部服务错误
- `08`: 数据格式错误
- `09`: 操作不允许

## 使用方式

### 方式1：直接导入枚举类（推荐）

```kotlin
import com.vgerbot.common.exception.CommonErrorCode
import com.vgerbot.auth.exception.AuthErrorCode

// 使用通用错误码
throw BusinessException(CommonErrorCode.COMMON_PARAM_INVALID)

// 使用认证模块错误码
throw BusinessException(AuthErrorCode.AUTH_USER_NOT_FOUND)

// 使用自定义消息
throw BusinessException(
    AuthErrorCode.AUTH_INVALID_CREDENTIALS,
    "用户名或密码错误，请重试"
)
```

### 方式2：通过扩展属性访问

```kotlin
import com.vgerbot.common.exception.ErrorCode

// 访问通用错误码
throw BusinessException(ErrorCode.Common.COMMON_PARAM_INVALID)

// 访问认证模块错误码
throw BusinessException(ErrorCode.Auth.AUTH_USER_NOT_FOUND)
```

### 方式3：使用异常类

```kotlin
import com.vgerbot.common.exception.NotFoundException
import com.vgerbot.common.exception.ConflictException
import com.vgerbot.common.exception.UnauthorizedException
import com.vgerbot.common.exception.ForbiddenException
import com.vgerbot.common.exception.ValidationException
import com.vgerbot.auth.exception.AuthErrorCode

// 资源未找到
throw NotFoundException(AuthErrorCode.AUTH_USER_NOT_FOUND)

// 资源冲突
throw ConflictException(AuthErrorCode.AUTH_USER_EXISTS)

// 未授权
throw UnauthorizedException(AuthErrorCode.AUTH_INVALID_CREDENTIALS)

// 权限不足
throw ForbiddenException()

// 参数验证
throw ValidationException("邮箱格式错误", "email")
```

## 创建新模块的错误码

### 1. 创建错误码枚举类

在模块的 `exception` 包下创建错误码枚举：

```kotlin
package com.vgerbot.yourmodule.exception

import com.vgerbot.common.exception.ErrorCode
import org.springframework.http.HttpStatus

/**
 * 您的模块错误码
 * 模块代码：XX（根据分配）
 */
enum class YourModuleErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) : ErrorCode {
    // 通用错误 (XX00XX)
    YOUR_MODULE_ERROR(XX0000, "模块错误", HttpStatus.BAD_REQUEST),
    
    // 参数验证错误 (XX01XX)
    YOUR_MODULE_PARAM_INVALID(XX0100, "参数验证失败", HttpStatus.BAD_REQUEST),
    
    // 资源不存在 (XX02XX)
    YOUR_MODULE_RESOURCE_NOT_FOUND(XX0200, "资源不存在", HttpStatus.NOT_FOUND),
    
    // ... 其他错误码
}
```

### 2. 在 ErrorCodeExtensions.kt 中添加扩展属性

```kotlin
/**
 * 扩展属性：您的模块错误码快捷访问
 */
val ErrorCode.Companion.YourModule: com.vgerbot.yourmodule.exception.YourModuleErrorCode.Companion
    get() = com.vgerbot.yourmodule.exception.YourModuleErrorCode.Companion
```

### 3. 在 ErrorCodes 中注册错误码

```kotlin
try {
    addAll(com.vgerbot.yourmodule.exception.YourModuleErrorCode.values())
} catch (e: ClassNotFoundException) {
    // 模块未加载时忽略
}
```

## API 响应格式

使用错误码后，API 响应会包含 `businessCode` 字段：

```json
{
  "code": 400,
  "businessCode": 200103,
  "message": "请求格式错误：必须提供 password 或 (encryptedPassword + keyId)",
  "details": {
    "field": "password"
  }
}
```

- `code`: HTTP 状态码
- `businessCode`: 业务错误码（6位数字）
- `message`: 错误消息
- `details`: 额外的错误详情（可选）

## 最佳实践

1. **优先使用模块专属错误码**：每个模块应使用自己的错误码枚举
2. **使用有意义的错误码**：错误码应能清晰表达错误的类型和原因
3. **保持错误码唯一性**：确保每个错误码在整个系统中唯一
4. **提供清晰的错误消息**：错误消息应该对用户友好
5. **使用异常类**：优先使用 `NotFoundException`、`ConflictException` 等语义化的异常类
6. **添加错误详情**：在需要时使用 `details` 参数提供额外的错误信息

## 示例

### 认证模块示例

```kotlin
import com.vgerbot.auth.exception.AuthErrorCode
import com.vgerbot.common.exception.BusinessException
import com.vgerbot.common.exception.UnauthorizedException

// 用户不存在
throw UnauthorizedException(AuthErrorCode.AUTH_USER_NOT_FOUND)

// 密码错误
throw UnauthorizedException(AuthErrorCode.AUTH_INVALID_CREDENTIALS)

// 密钥过期
throw UnauthorizedException(
    AuthErrorCode.AUTH_KEY_EXPIRED,
    "安全会话已过期，请重新获取公钥"
)
```

### PostgREST 查询模块示例

```kotlin
import com.vgerbot.postgrest.exception.PostgrestQueryErrorCode
import com.vgerbot.common.exception.BusinessException
import com.vgerbot.common.exception.ForbiddenException

// 表未注册
throw BusinessException(
    PostgrestQueryErrorCode.POSTGREST_QUERY_TABLE_NOT_REGISTERED,
    "表 '${tableName}' 未注册，无法查询"
)

// 权限不足
throw ForbiddenException(
    PostgrestQueryErrorCode.POSTGREST_QUERY_FORBIDDEN,
    "没有权限执行 ${operation} 操作 on 表 ${tableName}"
)
```

## 错误码查找

可以通过 `ErrorCode.findByCode()` 方法根据错误码查找对应的枚举：

```kotlin
import com.vgerbot.common.exception.ErrorCode

val errorCode = ErrorCode.findByCode(200200)
// 返回: AuthErrorCode.AUTH_USER_NOT_FOUND
```

## 注意事项

1. **模块解耦**：各模块的错误码枚举相互独立，不会产生编译时依赖
2. **延迟加载**：错误码枚举在首次使用时才会加载，避免循环依赖
3. **向后兼容**：仍然支持使用 HTTP 状态码创建异常（向后兼容）
4. **类型安全**：使用枚举类型确保错误码的类型安全

