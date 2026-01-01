# 字典数据校验功能文档

## 概述

字典模块提供了强大的数据校验功能，允许在字典类型定义时配置校验规则，在添加或更新字典数据时自动执行校验，确保数据的有效性和一致性。

## 核心特性

- ✅ **9种内置校验规则**：正则表达式、数值范围、长度、枚举、数字、日期范围、邮箱、手机号、URL
- ✅ **JSON 配置**：使用 JSON 格式配置校验规则，灵活易扩展
- ✅ **自定义错误消息**：支持自定义校验失败提示信息
- ✅ **自动校验**：创建和更新数据时自动执行校验
- ✅ **异常处理**：统一的异常处理机制，返回友好的错误信息
- ✅ **类型安全**：基于 Kotlin 密封类实现，类型安全有保障

## 校验规则类型

### 1. 正则表达式校验 (regex)

验证数据值是否符合指定的正则表达式。

**配置示例：**
```json
{
  "type": "regex",
  "pattern": "^[a-zA-Z0-9]+$"
}
```

**适用场景：**
- 用户名格式（字母数字组合）
- 邮政编码（特定格式）
- 身份证号码
- 自定义格式验证

**使用示例：**
```kotlin
val dictType = dictTypeService.createDictType(
    CreateDictTypeDto(
        dictCode = "username_format",
        dictName = "用户名格式",
        validationRule = """{"type": "regex", "pattern": "^[a-zA-Z0-9_]{3,20}$"}""",
        validationMessage = "用户名只能包含字母、数字和下划线，长度3-20位"
    )
)
```

### 2. 数值范围校验 (range)

验证数值是否在指定范围内。

**配置示例：**
```json
{
  "type": "range",
  "min": 0,
  "max": 100
}
```

**参数说明：**
- `min`: 最小值（可选）
- `max`: 最大值（可选）

**适用场景：**
- 年龄范围
- 百分比（0-100）
- 价格范围
- 数量限制

**使用示例：**
```kotlin
// 年龄：0-150
validationRule = """{"type": "range", "min": 0, "max": 150}"""

// 只限制最小值
validationRule = """{"type": "range", "min": 18}"""

// 只限制最大值
validationRule = """{"type": "range", "max": 999999}"""
```

### 3. 长度校验 (length)

验证字符串长度是否在指定范围内。

**配置示例：**
```json
{
  "type": "length",
  "minLength": 6,
  "maxLength": 20
}
```

**参数说明：**
- `minLength`: 最小长度（可选）
- `maxLength`: 最大长度（可选）

**适用场景：**
- 密码长度
- 验证码长度
- 昵称长度
- 描述字段长度

**使用示例：**
```kotlin
// 密码：8-32位
validationRule = """{"type": "length", "minLength": 8, "maxLength": 32}"""

// 验证码：固定6位
validationRule = """{"type": "length", "minLength": 6, "maxLength": 6}"""
```

### 4. 枚举值校验 (enum)

验证数据值是否在预定义的枚举列表中。

**配置示例：**
```json
{
  "type": "enum",
  "values": ["active", "inactive", "pending"]
}
```

**适用场景：**
- 状态值（有限的几个状态）
- 类型代码
- 固定选项值

**使用示例：**
```kotlin
validationRule = """{"type": "enum", "values": ["male", "female", "other"]}"""
```

### 5. 数字校验 (number)

验证是否为有效数字，支持整数、正数、负数限制。

**配置示例：**
```json
{
  "type": "number",
  "integerOnly": true,
  "positive": true
}
```

**参数说明：**
- `integerOnly`: 是否只允许整数（默认 false）
- `positive`: 是否只允许正数（默认 false）
- `negative`: 是否只允许负数（默认 false）

**适用场景：**
- 数量（正整数）
- 库存（正整数）
- 温度（可以是负数）
- 金额（正数，可以是小数）

**使用示例：**
```kotlin
// 正整数
validationRule = """{"type": "number", "integerOnly": true, "positive": true}"""

// 任意数字
validationRule = """{"type": "number"}"""

// 负数
validationRule = """{"type": "number", "negative": true}"""
```

### 6. 日期范围校验 (dateRange)

验证日期格式和范围。

**配置示例：**
```json
{
  "type": "dateRange",
  "format": "yyyy-MM-dd",
  "minDate": "2020-01-01",
  "maxDate": "2030-12-31"
}
```

**参数说明：**
- `format`: 日期格式（默认 "yyyy-MM-dd"）
- `minDate`: 最小日期（可选）
- `maxDate`: 最大日期（可选）

**适用场景：**
- 出生日期
- 有效期范围
- 活动日期范围

**使用示例：**
```kotlin
// 出生日期：1900-01-01 到今天
validationRule = """{"type": "dateRange", "format": "yyyy-MM-dd", "minDate": "1900-01-01", "maxDate": "2025-12-31"}"""

// 只验证格式
validationRule = """{"type": "dateRange", "format": "yyyy-MM-dd HH:mm:ss"}"""
```

### 7. 邮箱校验 (email)

验证是否为有效的邮箱地址。

**配置示例：**
```json
{
  "type": "email"
}
```

**使用示例：**
```kotlin
validationRule = """{"type": "email"}"""
validationMessage = "请输入有效的邮箱地址"
```

### 8. 手机号校验 (phone)

验证是否为有效的中国大陆手机号。

**配置示例：**
```json
{
  "type": "phone"
}
```

**规则：** 11位数字，以1开头，第二位为3-9

**使用示例：**
```kotlin
validationRule = """{"type": "phone"}"""
validationMessage = "请输入有效的手机号码"
```

### 9. URL 校验 (url)

验证是否为有效的 URL 地址。

**配置示例：**
```json
{
  "type": "url"
}
```

**支持的协议：** http, https, ftp

**使用示例：**
```kotlin
validationRule = """{"type": "url"}"""
validationMessage = "请输入有效的URL地址"
```

## 完整使用流程

### 1. 创建带校验规则的字典类型

```kotlin
val dictType = dictTypeService.createDictType(
    CreateDictTypeDto(
        dictCode = "user_age",
        dictName = "用户年龄",
        dictCategory = "user",
        valueType = "INTEGER",
        // 配置校验规则
        validationRule = """{"type": "range", "min": 0, "max": 150}""",
        // 配置自定义错误消息
        validationMessage = "年龄必须在0-150岁之间",
        status = true
    )
)
```

### 2. 添加字典数据（自动校验）

```kotlin
try {
    // 有效数据
    val data1 = dictDataService.createDictData(
        CreateDictDataDto(
            dictTypeId = dictType.id,
            dictCode = "user_age",
            dataValue = "25",  // ✓ 有效
            dataLabel = "青年"
        )
    )
    println("数据添加成功")
    
    // 无效数据
    val data2 = dictDataService.createDictData(
        CreateDictDataDto(
            dictTypeId = dictType.id,
            dictCode = "user_age",
            dataValue = "200",  // ✗ 超出范围
            dataLabel = "超龄"
        )
    )
} catch (e: DictValidationException) {
    // 捕获校验异常
    println("校验失败: ${e.message}")
    println("字典编码: ${e.dictCode}")
    println("数据值: ${e.dataValue}")
}
```

### 3. REST API 调用（自动校验）

```bash
# 创建带校验规则的字典类型
curl -X POST http://localhost:8080/api/dict/types \
  -H "Content-Type: application/json" \
  -d '{
    "dictCode": "email_contact",
    "dictName": "联系邮箱",
    "validationRule": "{\"type\": \"email\"}",
    "validationMessage": "请输入有效的邮箱地址"
  }'

# 添加有效数据
curl -X POST http://localhost:8080/api/dict/data \
  -H "Content-Type: application/json" \
  -d '{
    "dictTypeId": 1,
    "dictCode": "email_contact",
    "dataValue": "user@example.com",
    "dataLabel": "用户邮箱"
  }'
# 响应: 201 Created

# 添加无效数据
curl -X POST http://localhost:8080/api/dict/data \
  -H "Content-Type: application/json" \
  -d '{
    "dictTypeId": 1,
    "dictCode": "email_contact",
    "dataValue": "invalid-email",
    "dataLabel": "无效邮箱"
  }'
# 响应: 400 Bad Request
# {
#   "error": "VALIDATION_ERROR",
#   "message": "请输入有效的邮箱地址",
#   "dictCode": "email_contact",
#   "dataValue": "invalid-email"
# }
```

## 异常处理

### DictValidationException

当数据校验失败时抛出此异常。

**异常属性：**
- `message`: 错误消息
- `dictCode`: 字典编码
- `dataValue`: 校验失败的数据值
- `validationRule`: 校验规则（JSON）

**全局异常处理：**

字典模块提供了全局异常处理器 `DictExceptionHandler`，自动将校验异常转换为友好的 HTTP 响应：

```json
{
  "error": "VALIDATION_ERROR",
  "message": "用户名只能包含字母和数字",
  "dictCode": "username_format",
  "dataValue": "user@123",
  "validationRule": "{\"type\": \"regex\", \"pattern\": \"^[a-zA-Z0-9]+$\"}"
}
```

## 实战案例

### 案例1：用户注册表单验证

```kotlin
// 1. 创建用户名格式字典
dictTypeService.createDictType(
    CreateDictTypeDto(
        dictCode = "reg_username",
        dictName = "注册用户名",
        validationRule = """{"type": "regex", "pattern": "^[a-zA-Z][a-zA-Z0-9_]{2,19}$"}""",
        validationMessage = "用户名必须以字母开头，可包含字母数字下划线，长度3-20位"
    )
)

// 2. 创建密码强度字典
dictTypeService.createDictType(
    CreateDictTypeDto(
        dictCode = "reg_password",
        dictName = "注册密码",
        validationRule = """{"type": "regex", "pattern": "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$"}""",
        validationMessage = "密码至少8位，必须包含字母和数字"
    )
)

// 3. 创建邮箱字典
dictTypeService.createDictType(
    CreateDictTypeDto(
        dictCode = "reg_email",
        dictName = "注册邮箱",
        validationRule = """{"type": "email"}""",
        validationMessage = "请输入有效的邮箱地址"
    )
)

// 4. 在注册流程中使用
fun validateRegistration(username: String, password: String, email: String): Boolean {
    return try {
        val usernameType = dictTypeService.getDictTypeByCode("reg_username")!!
        val passwordType = dictTypeService.getDictTypeByCode("reg_password")!!
        val emailType = dictTypeService.getDictTypeByCode("reg_email")!!
        
        dictValidator.validateOrThrow(usernameType, username)
        dictValidator.validateOrThrow(passwordType, password)
        dictValidator.validateOrThrow(emailType, email)
        
        true
    } catch (e: DictValidationException) {
        println("注册信息校验失败: ${e.message}")
        false
    }
}
```

### 案例2：商品信息验证

```kotlin
// SKU 编码格式
dictTypeService.createDictType(
    CreateDictTypeDto(
        dictCode = "product_sku",
        dictName = "商品SKU",
        validationRule = """{"type": "regex", "pattern": "^[A-Z]{2}\\d{8}$"}""",
        validationMessage = "SKU格式：2位大写字母+8位数字"
    )
)

// 商品价格范围
dictTypeService.createDictType(
    CreateDictTypeDto(
        dictCode = "product_price",
        dictName = "商品价格",
        validationRule = """{"type": "range", "min": 0.01, "max": 999999.99}""",
        validationMessage = "价格范围：0.01-999999.99元"
    )
)

// 库存数量（正整数）
dictTypeService.createDictType(
    CreateDictTypeDto(
        dictCode = "product_stock",
        dictName = "库存数量",
        validationRule = """{"type": "number", "integerOnly": true, "positive": true}""",
        validationMessage = "库存必须是正整数"
    )
)
```

### 案例3：活动时间验证

```kotlin
dictTypeService.createDictType(
    CreateDictTypeDto(
        dictCode = "activity_date",
        dictName = "活动日期",
        validationRule = """{"type": "dateRange", "format": "yyyy-MM-dd", "minDate": "2024-01-01", "maxDate": "2025-12-31"}""",
        validationMessage = "活动日期必须在2024-2025年之间"
    )
)
```

## 最佳实践

### 1. 合理选择校验规则

- **简单场景**: 使用内置规则（email, phone, url等）
- **复杂场景**: 使用正则表达式规则
- **范围限制**: 使用 range 或 number 规则
- **固定选项**: 使用 enum 规则

### 2. 提供友好的错误消息

```kotlin
// ❌ 不友好
validationMessage = "值不符合正则表达式规则: ^[a-zA-Z0-9]+$"

// ✅ 友好
validationMessage = "用户名只能包含字母和数字"
```

### 3. 校验规则版本管理

如果校验规则需要变更，建议创建新的字典类型，而不是修改现有规则，以保持历史数据的一致性。

### 4. 性能优化

- 校验规则会在每次创建/更新数据时执行
- 对于高频操作的字典，建议使用简单的校验规则
- 复杂的正则表达式可能影响性能

### 5. 错误处理

在业务代码中合理捕获 `DictValidationException`：

```kotlin
try {
    dictDataService.createDictData(dto)
} catch (e: DictValidationException) {
    // 记录日志
    logger.warn("Dict validation failed", e)
    // 返回用户友好的错误信息
    return ResponseEntity.badRequest().body(e.message)
}
```

## 扩展校验规则

如需添加自定义校验规则，可以扩展 `ValidationRule` 密封类：

```kotlin
// 1. 在 ValidationRule.kt 中添加新规则
data class CustomValidationRule(
    val param1: String,
    val param2: Int
) : ValidationRule() {
    override fun validate(value: String): Boolean {
        // 自定义校验逻辑
        return true
    }
    
    override fun getDefaultMessage(): String {
        return "自定义校验失败"
    }
}

// 2. 在 @JsonSubTypes 注解中注册
@JsonSubTypes(
    // ... 其他规则
    JsonSubTypes.Type(value = CustomValidationRule::class, name = "custom")
)

// 3. 使用
validationRule = """{"type": "custom", "param1": "value1", "param2": 123}"""
```

## 常见问题

### Q1: 如何禁用某个字典的校验？

**A:** 不设置 `validationRule` 或设置为空字符串即可。

### Q2: 可以组合多个校验规则吗？

**A:** 当前版本暂不支持。如需组合校验，建议使用正则表达式实现。

### Q3: 校验失败后数据会被保存吗？

**A:** 不会。校验在数据保存之前执行，失败会抛出异常，事务会回滚。

### Q4: 如何测试校验规则？

**A:** 可以使用 `DictValidator.validate()` 方法测试，返回 `ValidationResult`：

```kotlin
val result = dictValidator.validate(dictType, testValue)
if (!result.isValid) {
    println("校验失败: ${result.message}")
}
```

## 总结

数据校验功能为字典模块提供了强大的数据质量保障能力。通过合理配置校验规则，可以：

- ✅ 保证数据格式的一致性
- ✅ 防止无效数据入库
- ✅ 提供友好的错误提示
- ✅ 减少业务层的校验代码
- ✅ 实现配置化的数据规则管理

建议在生产环境中为关键字典配置适当的校验规则，提升系统的数据质量和可靠性。


