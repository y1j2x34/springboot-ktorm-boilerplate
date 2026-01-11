# Infrastructure Redis 模块

Redis 基础设施模块，提供分布式锁和缓存功能。

## 功能特性

### 1. 分布式锁（RedisLockService）

- ✅ 基于 Redis 的分布式锁实现
- ✅ 支持锁的超时自动释放
- ✅ 支持锁的续期
- ✅ 使用 Lua 脚本保证原子性操作
- ✅ 防止误释放其他进程的锁

### 2. 缓存服务（RedisCacheService）

- ✅ 键值对缓存
- ✅ 支持过期时间设置
- ✅ 防止缓存穿透（缓存 null 值）
- ✅ 批量操作支持
- ✅ 缓存获取或设置（getOrSet）

## 配置

在 `application.yml` 或环境变量中配置 Redis 连接：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0

vgerbot:
  redis:
    lock-default-timeout: 30      # 分布式锁默认过期时间（秒）
    cache-default-timeout: 3600   # 缓存默认过期时间（秒）
    lock-retry-interval: 100      # 锁重试间隔（毫秒）
    lock-max-retries: 10          # 锁最大重试次数
```

## 使用示例

### 分布式锁使用

```kotlin
@Service
class OrderService(
    private val redisLockService: RedisLockService
) {
    fun processOrder(orderId: String) {
        val lockKey = "order:process:$orderId"
        
        // 方式1：尝试获取锁（非阻塞）
        val lockValue = redisLockService.tryLock(lockKey, timeoutSeconds = 60)
        if (lockValue != null) {
            try {
                // 执行业务逻辑
                doProcessOrder(orderId)
            } finally {
                redisLockService.unlock(lockKey, lockValue)
            }
        } else {
            throw BusinessException("订单正在处理中，请稍候")
        }
        
        // 方式2：获取锁（阻塞，带超时）
        val lockValue2 = redisLockService.lock(lockKey, timeoutSeconds = 60, waitTimeoutSeconds = 5)
        if (lockValue2 != null) {
            try {
                doProcessOrder(orderId)
            } finally {
                redisLockService.unlock(lockKey, lockValue2)
            }
        }
        
        // 方式3：使用 executeWithLock（推荐）
        redisLockService.executeWithLock(lockKey, timeoutSeconds = 60) {
            doProcessOrder(orderId)
        } ?: throw BusinessException("获取锁失败")
    }
    
    private fun doProcessOrder(orderId: String) {
        // 业务逻辑
    }
}
```

### 缓存使用

```kotlin
@Service
class UserService(
    private val redisCacheService: RedisCacheService
) {
    fun getUserById(userId: String): User? {
        val cacheKey = "user:$userId"
        
        // 方式1：手动缓存
        val cached = redisCacheService.get<User>(cacheKey)
        if (cached != null) {
            return cached
        }
        
        val user = userDao.findById(userId)
        redisCacheService.set(cacheKey, user, timeoutSeconds = 3600)
        return user
        
        // 方式2：使用 getOrSet（推荐）
        return redisCacheService.getOrSet(cacheKey, timeoutSeconds = 3600) {
            userDao.findById(userId)
        }
    }
    
    fun updateUser(user: User) {
        userDao.update(user)
        // 更新后清除缓存
        redisCacheService.delete("user:${user.id}")
    }
    
    fun getUserCount(): Long {
        val cacheKey = "user:count"
        return redisCacheService.getOrSet(cacheKey, timeoutSeconds = 300) {
            userDao.count()
        }
    }
    
    fun incrementUserViewCount(userId: String) {
        val cacheKey = "user:view:$userId"
        redisCacheService.increment(cacheKey, delta = 1)
    }
}
```

### 批量操作

```kotlin
@Service
class CacheManagementService(
    private val redisCacheService: RedisCacheService
) {
    fun clearUserCache(userId: String) {
        // 清除单个缓存
        redisCacheService.delete("user:$userId")
    }
    
    fun clearAllUserCache() {
        // 清除所有用户相关缓存
        redisCacheService.clear("user:*")
    }
    
    fun clearAllCache() {
        // 清除所有缓存（谨慎使用）
        redisCacheService.clear()
    }
}
```

## 注意事项

1. **分布式锁**：
   - 锁的 `lockValue` 必须保存，用于释放锁时验证
   - 建议使用 `executeWithLock` 方法，自动管理锁的生命周期
   - 锁的超时时间应该大于业务执行时间

2. **缓存**：
   - 缓存 null 值会使用较短的过期时间（60秒）防止缓存穿透
   - 使用 `getOrSet` 可以避免缓存击穿问题
   - 批量删除操作在生产环境需谨慎使用

3. **性能**：
   - Redis 操作是网络 IO，注意异常处理
   - 大量数据操作建议使用批量接口

