# 异步任务处理模块

通用的异步任务处理框架，支持任务提交、后台处理、状态跟踪、优先级、重试机制和通知集成。

## 功能特性

- ✅ 异步任务提交：立即返回，后台处理
- ✅ 任务状态跟踪：PENDING、PROCESSING、SUCCESS、FAILURE、RETRYING
- ✅ 优先级支持：数字越大优先级越高
- ✅ 可配置重试：支持固定间隔和指数退避两种策略
- ✅ 并发处理：可配置并发线程数
- ✅ 分布式锁：使用 Redis 防止多实例重复处理
- ✅ 通知集成：任务完成时可发送通知
- ✅ 超时恢复：自动恢复超时的 PROCESSING 状态任务

## 快速开始

### 1. 配置

在 `application.yml` 中添加配置：

```yaml
async-task:
  consumer:
    enabled: true
    poll-interval: 5  # 轮询间隔（秒）
    batch-size: 10    # 每次处理的任务数
    concurrency: 3   # 并发处理线程数
    max-processing-time: 3600  # 最大处理时间（秒）
```

### 2. 实现任务处理器

创建一个实现 `TaskProcessor` 接口的组件：

```kotlin
@Component
class CreateServerTaskProcessor : TaskProcessor<CreateServerPayload> {
    
    override fun getTaskType() = "CREATE_SERVER"
    
    override fun process(task: AsyncTask, payload: CreateServerPayload): TaskResult {
        // 实际创建云服务器的逻辑
        val serverId = cloudServerService.create(payload)
        return TaskResult.success(mapOf("serverId" to serverId))
    }
    
    override fun getRetryConfig() = RetryConfig(
        maxRetryCount = 3,
        strategy = RetryStrategy.EXPONENTIAL,
        baseInterval = 60
    )
    
    override fun getPriority(payload: CreateServerPayload) = payload.priority
    
    override fun shouldNotifyOnSuccess() = true
    override fun shouldNotifyOnFailure() = true
    
    override fun buildNotificationRequest(task: AsyncTask, result: TaskResult): NotificationRequest? {
        return NotificationRequest(
            type = NotificationType.EMAIL,
            recipient = task.createdBy?.let { getUserEmail(it) } ?: return null,
            subject = "云服务器创建${if (result.success) "成功" else "失败"}",
            content = if (result.success) {
                "您的云服务器已创建成功，服务器ID: ${result.data?.get("serverId")}"
            } else {
                "云服务器创建失败: ${result.message}"
            }
        )
    }
}
```

### 3. 提交任务

在 Controller 或 Service 中使用：

```kotlin
@RestController
class ServerController(
    private val asyncTaskService: AsyncTaskService
) {
    
    @PostMapping("/servers")
    fun createServer(@RequestBody dto: CreateServerDto): ApiResponse<Long> {
        val task = asyncTaskService.submitTask(
            taskType = "CREATE_SERVER",
            payload = CreateServerPayload(
                name = dto.name,
                config = dto.config,
                priority = 10
            ),
            priority = 10
        )
        return ApiResponse.success(task.id)
    }
    
    @GetMapping("/servers/tasks/{taskId}")
    fun getTaskStatus(@PathVariable taskId: Long): ApiResponse<AsyncTask> {
        val task = asyncTaskService.getTask(taskId)
        return if (task != null) {
            ApiResponse.success(task)
        } else {
            ApiResponse.error("任务不存在")
        }
    }
}
```

## 任务状态

- **PENDING**: 待处理，等待消费者处理
- **PROCESSING**: 处理中，正在执行
- **SUCCESS**: 成功，任务执行成功
- **FAILURE**: 失败，任务执行失败且无法重试
- **RETRYING**: 重试中，任务失败但可以重试

## 重试策略

### 固定间隔（FIXED）

每次重试的间隔时间固定。

```kotlin
RetryConfig(
    maxRetryCount = 3,
    strategy = RetryStrategy.FIXED,
    baseInterval = 60  // 每次重试间隔 60 秒
)
```

### 指数退避（EXPONENTIAL）

重试间隔按指数增长：baseInterval * 2^retryCount

```kotlin
RetryConfig(
    maxRetryCount = 3,
    strategy = RetryStrategy.EXPONENTIAL,
    baseInterval = 60  // 第1次重试：60秒，第2次：120秒，第3次：240秒
)
```

## API 参考

### AsyncTaskService

- `submitTask()`: 提交任务
- `getTask()`: 根据ID获取任务
- `getTasksByType()`: 根据类型和状态查询任务列表
- `cancelTask()`: 取消任务（只能取消 PENDING 或 RETRYING 状态）
- `retryTask()`: 手动重试任务（只能重试 FAILURE 状态）

### TaskProcessor

- `getTaskType()`: 返回任务类型标识
- `process()`: 处理任务的核心逻辑
- `getRetryConfig()`: 返回重试配置（可选）
- `getPriority()`: 根据 payload 动态计算优先级（可选）
- `shouldNotifyOnSuccess()`: 是否在成功时发送通知
- `shouldNotifyOnFailure()`: 是否在失败时发送通知
- `buildNotificationRequest()`: 构建通知请求（可选）

## 注意事项

1. **任务处理器注册**：确保 `TaskProcessor` 实现类被 Spring 管理（使用 `@Component` 等注解）

2. **分布式锁**：如果部署了多个实例，建议配置 Redis 以启用分布式锁，防止重复处理

3. **任务超时**：如果任务处理时间超过 `max-processing-time`，任务会被重置为 PENDING 状态

4. **重试延迟**：当前实现中，RETRYING 状态的任务会在下次轮询时立即重试。如需延迟重试，可以通过配置 `poll-interval` 来控制

5. **事务管理**：任务处理在事务中执行，确保数据一致性

6. **错误处理**：任务处理过程中的异常会被捕获并记录，不会影响其他任务的处理

## 数据库表结构

表名：`async_task`

主要字段：
- `id`: 主键
- `task_type`: 任务类型
- `status`: 任务状态
- `priority`: 优先级
- `payload`: 任务数据（JSON）
- `result`: 执行结果（JSON）
- `retry_count`: 当前重试次数
- `max_retry_count`: 最大重试次数
- `retry_strategy`: 重试策略
- `retry_interval`: 重试间隔（秒）

详见 `database/init/16_async_task.sql`

