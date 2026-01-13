# 通知模块 (Notification Module)

通知模块提供了统一的通知发送接口，支持邮件、短信等多种通知方式。模块采用提供商模式设计，支持动态配置和扩展，方便对接不同的通知服务提供商。

## 模块结构

```
notification/
├── notification-api/          # API 接口层
│   ├── NotificationType.kt    # 通知类型枚举
│   ├── ProviderType.kt        # 提供商类型枚举
│   ├── NotificationStatus.kt  # 通知状态枚举
│   ├── NotificationResult.kt  # 通知结果
│   ├── provider/              # 提供商接口
│   │   ├── NotificationProvider.kt
│   │   └── NotificationRequest.kt
│   ├── service/               # 服务接口
│   │   └── NotificationService.kt
│   └── dto/                   # 数据传输对象
│       ├── SendNotificationDto.kt
│       └── NotificationRecordDto.kt
└── notification-core/         # 核心实现层
    ├── config/                # 配置类
    │   ├── NotificationProperties.kt
    │   ├── ProviderConfig.kt
    │   └── NotificationAutoConfiguration.kt
    ├── provider/              # 提供商实现
    │   ├── ProviderManager.kt
    │   ├── AbstractNotificationProvider.kt
    │   └── impl/
    │       └── PlaceholderProvider.kt
    ├── service/               # 服务实现
    │   └── NotificationServiceImpl.kt
    ├── controller/            # REST 控制器
    │   └── NotificationController.kt
    └── exception/             # 异常和错误码
        └── NotificationErrorCode.kt
```

## 核心功能

### 1. 通知类型

模块支持以下通知类型：

- **EMAIL**: 邮件通知
- **SMS**: 短信通知
- **IN_APP**: 站内消息
- **PUSH**: 推送通知
- **WECHAT**: 微信消息
- **OTHER**: 其他类型（可扩展）

### 2. 提供商类型

模块支持以下提供商类型（可扩展）：

- **SMTP**: SMTP 邮件服务器
- **ALIYUN_SMS**: 阿里云短信服务
- **TENCENT_SMS**: 腾讯云短信服务
- **HUAWEI_SMS**: 华为云短信服务
- **CUSTOM**: 自定义提供商

### 3. 统一的服务接口

`NotificationService` 提供了统一的通知发送接口：

```kotlin
// 发送通知
fun send(request: NotificationRequest): NotificationResult

// 批量发送通知
fun sendBatch(requests: List<NotificationRequest>): List<NotificationResult>

// 发送邮件（便捷方法）
fun sendEmail(to: String, subject: String, content: String): NotificationResult

// 发送短信（便捷方法）
fun sendSms(to: String, content: String, templateId: String?): NotificationResult

// 检查通知类型是否可用
fun isTypeAvailable(type: NotificationType): Boolean
```

## 使用方式

### 1. 添加依赖

在你的模块 `build.gradle.kts` 中添加：

```kotlin
dependencies {
    implementation(project(":modules:notification:notification-core"))
}
```

### 2. 配置提供商

在 `application.yml` 中配置：

```yaml
notification:
  enabled: true
  default-provider: SMTP
  record-history: true
  async:
    enabled: true
    core-pool-size: 5
    max-pool-size: 10
  retry:
    enabled: true
    max-attempts: 3
    interval: 1000
```

### 3. 实现提供商

要实现具体的通知提供商，需要：

1. 继承 `AbstractNotificationProvider` 或实现 `NotificationProvider` 接口
2. 使用 `@Component` 注解注册为 Spring Bean
3. 实现 `doSend()` 方法

示例：

```kotlin
@Component
class SmtpEmailProvider(
    private val mailProperties: MailProperties
) : AbstractNotificationProvider(
    providerTypeValue = ProviderType.SMTP,
    supportedTypesValue = setOf(NotificationType.EMAIL)
) {
    
    override fun checkAvailability(): Boolean {
        // 检查SMTP配置是否完整
        return mailProperties.host.isNotBlank() && 
               mailProperties.username.isNotBlank()
    }
    
    override fun doSend(request: NotificationRequest): NotificationResult {
        // 实现SMTP发送逻辑
        // ...
        return NotificationResult.success(
            message = "邮件发送成功",
            providerMessageId = "smtp-${System.currentTimeMillis()}"
        )
    }
}
```

### 4. 在代码中使用

#### 方式一：通过服务接口使用（推荐）

```kotlin
@Service
class UserService(
    private val notificationService: NotificationService
) {
    fun sendWelcomeEmail(user: User) {
        val result = notificationService.sendEmail(
            to = user.email,
            subject = "欢迎注册",
            content = "欢迎您注册我们的服务！"
        )
        
        if (!result.success) {
            logger.error("发送欢迎邮件失败: {}", result.message)
        }
    }
    
    fun sendVerificationCode(phone: String, code: String) {
        val result = notificationService.sendSms(
            to = phone,
            content = "您的验证码是：$code，5分钟内有效",
            templateId = "SMS_VERIFICATION_CODE"
        )
        
        if (!result.success) {
            logger.error("发送验证码失败: {}", result.message)
        }
    }
}
```

#### 方式二：通过 REST API 使用

```bash
# 发送邮件
curl -X POST http://localhost:8080/notification/email \
  -d "to=user@example.com" \
  -d "subject=测试邮件" \
  -d "content=这是一封测试邮件"

# 发送短信
curl -X POST http://localhost:8080/notification/sms \
  -d "to=13800138000" \
  -d "content=您的验证码是123456"

# 发送通用通知
curl -X POST http://localhost:8080/notification/send \
  -H "Content-Type: application/json" \
  -d '{
    "type": "EMAIL",
    "recipient": "user@example.com",
    "subject": "测试",
    "content": "测试内容"
  }'

# 检查通知类型是否可用
curl http://localhost:8080/notification/type/EMAIL/available
```

## 数据库表结构

模块提供了两个数据库表：

1. **notification_provider**: 存储提供商配置信息
2. **notification_record**: 存储通知发送记录

执行 `database/init/15_notification_provider.sql` 创建表结构。

## 扩展性设计

### 1. 添加新的通知类型

在 `NotificationType` 枚举中添加新类型即可。

### 2. 添加新的提供商

1. 在 `ProviderType` 枚举中添加新类型
2. 实现 `NotificationProvider` 接口
3. 使用 `@Component` 注册为 Bean
4. 系统会自动发现并注册新的提供商

### 3. 动态配置提供商

通过 `ProviderManager.registerProviderConfig()` 方法可以动态注册提供商配置（预留接口）。

## 配置说明

### NotificationProperties

- `enabled`: 是否启用通知模块（默认：true）
- `defaultProvider`: 默认提供商类型
- `recordHistory`: 是否记录发送历史（默认：true）
- `async.enabled`: 是否启用异步发送（默认：true）
- `async.corePoolSize`: 线程池核心线程数（默认：5）
- `async.maxPoolSize`: 线程池最大线程数（默认：10）
- `retry.enabled`: 是否启用重试（默认：true）
- `retry.maxAttempts`: 最大重试次数（默认：3）
- `retry.interval`: 重试间隔（毫秒，默认：1000）

## 错误码

模块错误码范围：950000-959999

- `950000`: 通知错误
- `950100`: 通知参数验证失败
- `950101`: 接收者格式无效
- `950102`: 通知内容不能为空
- `950200`: 通知记录不存在
- `950201`: 通知提供商不存在
- `950600`: 不支持的通知类型
- `950601`: 通知提供商不可用
- `950602`: 通知提供商未配置
- `950603`: 通知发送失败
- `950700`: 通知提供商服务错误
- `950701`: 通知提供商服务超时

## 注意事项

1. **提供商实现**: 当前模块只提供了占位符提供商示例，实际使用时需要实现具体的提供商（如SMTP、阿里云SMS等）
2. **配置安全**: 提供商的鉴权配置（如API密钥）应该存储在安全的地方，建议使用配置中心或加密存储
3. **异步发送**: 默认启用异步发送，提高性能，但需要注意异常处理
4. **重试机制**: 发送失败时会自动重试，重试次数和间隔可配置
5. **记录历史**: 建议启用历史记录功能，便于追踪和排查问题

## 后续扩展建议

1. **实现具体提供商**: 
   - SMTP邮件提供商
   - 阿里云短信提供商
   - 腾讯云短信提供商
   - 其他第三方服务提供商

2. **模板支持**: 
   - 实现通知模板功能
   - 支持变量替换
   - 模板管理接口

3. **队列支持**: 
   - 集成消息队列（如RabbitMQ、Kafka）
   - 支持延迟发送
   - 支持优先级队列

4. **统计功能**: 
   - 发送成功率统计
   - 提供商性能监控
   - 通知类型使用统计

5. **管理界面**: 
   - 提供商配置管理
   - 发送记录查询
   - 模板管理

