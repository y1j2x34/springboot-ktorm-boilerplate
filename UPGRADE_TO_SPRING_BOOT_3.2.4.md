# Spring Boot 3.2.4 版本升级指南

## 📋 升级概览

本次升级将项目从 Spring Boot 2.7.1 统一升级到 Spring Boot 3.2.4，并解决了所有版本冲突和兼容性问题。

## ✅ 已完成的修改

### 1. 核心依赖版本统一

#### `build.gradle.kts`
- ✅ Spring Boot BOM: `2.7.1` → `3.2.4`
- ✅ Kotlin 版本: `1.8.20` → `1.9.23`
- ✅ kotlin-reflect: 移除硬编码版本，由 BOM 管理
- ✅ MariaDB JDBC: `3.1.3` → `3.4.0`
- ❌ 移除 `javax.xml.bind:jaxb-api`（Spring Boot 3.x 不再需要）

### 2. 子模块依赖优化与配置

#### 库模块 bootJar 禁用
所有库模块（非可执行模块）都需要禁用 `bootJar` 任务：
- ✅ `common/build.gradle.kts`
- ✅ `user/build.gradle.kts`
- ✅ `jwt-auth/build.gradle.kts`
- ✅ `rbac/build.gradle.kts`
- ✅ `tenant/build.gradle.kts`
- ✅ `captcha/build.gradle.kts`（已有配置）

```kotlin
tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}
```

#### `user/build.gradle.kts`
- ✅ Spring Security Crypto: 移除硬编码版本 `5.7.2`，由 BOM 管理

#### `app/build.gradle.kts`
- ✅ Reactor Core: 移除硬编码版本 `3.2.8.RELEASE`，由 BOM 管理
- ✅ 添加 Spring Security 显式依赖（AppSecurityConfiguration 需要）

#### `jwt-auth/build.gradle.kts`
- ✅ JWT 库升级: `io.jsonwebtoken:jjwt:0.9.1` → `0.12.5`
- ✅ 拆分为三个依赖:
  - `io.jsonwebtoken:jjwt-api:0.12.5`
  - `io.jsonwebtoken:jjwt-impl:0.12.5` (runtime)
  - `io.jsonwebtoken:jjwt-jackson:0.12.5` (runtime)

### 3. Jakarta EE 命名空间迁移

Spring Boot 3.x 使用 Jakarta EE 9+，所有 `javax.*` 包需要迁移到 `jakarta.*`：

#### 已修复的文件
- ✅ `common/src/main/kotlin/com/vgerbot/common/security/UserIdentityResolver.kt`
- ✅ `jwt-auth/src/main/kotlin/com/vgerbot/auth/JwtRequestFilter.kt`
- ✅ `jwt-auth/src/main/kotlin/com/vgerbot/auth/JwtAuthenticationEntryPoint.kt`
- ✅ `jwt-auth/src/main/kotlin/com/vgerbot/auth/JwtUserIdentityResolver.kt`
- ✅ `rbac/src/main/kotlin/com/vgerbot/rbac/interceptor/RbacInterceptor.kt`
- ✅ `rbac/src/main/kotlin/com/vgerbot/rbac/interceptor/RbacPermissionInterceptor.kt`
- ✅ `rbac/src/main/kotlin/com/vgerbot/rbac/interceptor/JwtRbacInterceptor.kt`
- ✅ `tenant/src/main/kotlin/com/vgerbot/tenant/filter/TenantAuthenticationFilter.kt`
- ✅ `tenant/src/main/kotlin/com/vgerbot/tenant/configuration/TenantAutoConfiguration.kt`

#### 迁移详情
```kotlin
// 旧包名 (Spring Boot 2.x)
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.FilterChain
import javax.annotation.PostConstruct

// 新包名 (Spring Boot 3.x)
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.FilterChain
import jakarta.annotation.PostConstruct
```

### 4. Spring Security API 升级

#### `jwt-auth/src/main/kotlin/com/vgerbot/auth/JwtConfiguration.kt`

**废弃 API 替换：**

```kotlin
// 旧 API (Spring Security 5.x)
@EnableGlobalMethodSecurity(prePostEnabled = true)
http.authorizeRequests {
    it.antMatchers(HttpMethod.POST, "/public/**").permitAll()
}

// 新 API (Spring Security 6.x)
@EnableMethodSecurity(prePostEnabled = true)
http.authorizeHttpRequests { authorize ->
    authorize.requestMatchers(HttpMethod.POST, "/public/**").permitAll()
}
```

**配置风格现代化：**

```kotlin
// 旧风格
http.run {
    cors().and().csrf().disable()
    // ...
}.build()

// 新风格（Lambda DSL）
http
    .cors { it.disable() }
    .csrf { it.disable() }
    // ...
    .build()
```

#### `app/src/main/kotlin/com/vgerbot/app/configuration/AppSecurityConfiguration.kt`
- ✅ 同样的 API 升级
- ✅ 保持租户过滤器集成

### 5. JWT Token 工具类升级

#### `jwt-auth/src/main/kotlin/com/vgerbot/auth/JwtTokenUtils.kt`

**JJWT 0.12.x 新 API：**

```kotlin
// 旧 API (JJWT 0.9.x)
Jwts.parser()
    .setSigningKey(properties.secret)
    .parseClaimsJws(token)
    .getBody()

Jwts.builder()
    .setClaims(claims)
    .setSubject(subject)
    .signWith(SignatureAlgorithm.HS512, properties.secret)
    .compact()

// 新 API (JJWT 0.12.x)
Jwts.parser()
    .verifyWith(getSigningKey())
    .build()
    .parseSignedClaims(token)
    .payload

Jwts.builder()
    .claims(claims)
    .subject(subject)
    .signWith(getSigningKey())
    .compact()
```

**密钥处理改进：**

```kotlin
private fun getSigningKey(): SecretKey {
    val keyBytes = properties.secret.toByteArray(StandardCharsets.UTF_8)
    return Keys.hmacShaKeyFor(keyBytes)
}
```

### 6. Configuration Properties 简化

#### `jwt-auth/src/main/kotlin/com/vgerbot/auth/JwtConfigurationProperties.kt`

```kotlin
// 旧方式 (Spring Boot 2.x)
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties @ConstructorBinding constructor(
    val secret: String
)

// 新方式 (Spring Boot 3.x)
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String
)
```

- ✅ 移除 `@ConstructorBinding` 注解（Spring Boot 3.x 自动支持构造函数绑定）

### 7. 日志 API 修复

#### `tenant/src/main/kotlin/com/vgerbot/tenant/filter/TenantAuthenticationFilter.kt`

```kotlin
// 从 SLF4J 参数化日志改为 Kotlin 字符串模板
// 旧方式
logger.debug("Found tenant for user {}: {}", username, tenant.name)

// 新方式
logger.debug("Found tenant for user $username: ${tenant.name}")
```

## 🔍 验证检查项

### 编译验证
```bash
./gradlew clean build
```

✅ **编译结果**: BUILD SUCCESSFUL (已验证)

### 编译警告（非阻塞性）
以下警告不影响功能，可选择性修复：
1. `Base64Utils` 已废弃（captcha 模块）- 建议迁移到 `java.util.Base64`
2. Kotlin logger 属性遮蔽警告（tenant 模块）- Kotlin 1.9.x 与 Spring 过滤器的兼容性问题
3. 未使用的参数警告 - 代码清理优化项

### 关键功能测试
- [ ] JWT 认证功能
- [ ] RBAC 权限检查
- [ ] 租户隔离功能
- [ ] 验证码功能

### 运行时验证
```bash
./gradlew :app:bootRun
```

### 8. 修复配置冲突

#### Spring Boot 3.x 不允许排除非自动配置类

**问题**: `@SpringBootApplication(exclude = [JwtConfiguration::class])` 在 Spring Boot 3.x 中报错

**原因**: Spring Boot 3.x 的 `exclude` 只能用于 `@AutoConfiguration` 注解的类

**解决方案**:
1. 移除 `Application.kt` 中的 `exclude = [JwtConfiguration::class]`
2. 在 `JwtConfiguration.configure()` 方法上添加 `@ConditionalOnMissingBean(DefaultSecurityFilterChain::class)`

这样当 `AppSecurityConfiguration` 定义了 Security 过滤器链时，`JwtConfiguration` 的配置会自动失效。

## ⚠️ 注意事项

### 1. 数据库驱动兼容性
- MariaDB JDBC 驱动已升级到 3.4.0
- 确保数据库版本兼容（MariaDB 10.2+）

### 2. JWT Secret 密钥长度
JJWT 0.12.x 对 HS512 算法要求密钥至少 **512 位（64 字节）**：

```yaml
jwt:
  secret: "your-secret-key-must-be-at-least-512-bits-long-for-hs512-algorithm-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
```

### 3. Java 版本要求
- Spring Boot 3.2.4 要求 **Java 17+**
- 已配置 `jvmTarget = "17"`

### 4. 已知的 API 变更
- `HttpServletRequest.getSession(false)` 行为不变
- Spring Security 的 `Authentication` 对象结构未变
- `UserDetails` 接口保持向后兼容

## 📦 依赖树总结

```
Spring Boot 3.2.4 BOM
├── Spring Framework 6.1.x
├── Spring Security 6.2.x
├── Kotlin 1.9.23
├── JJWT 0.12.5
├── Ktorm 3.6.0 (无变更)
└── MariaDB JDBC 3.4.0
```

## 🚀 后续优化建议

1. **测试覆盖**: 添加单元测试和集成测试
2. **缓存优化**: 为 RBAC 权限检查添加 Redis 缓存
3. **日志优化**: 统一使用 SLF4J，考虑使用结构化日志
4. **配置外部化**: 敏感配置使用环境变量或配置中心
5. **监控增强**: 集成 Spring Boot Actuator 和 Micrometer

## 📚 参考文档

- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Spring Security 6.0 Migration Guide](https://docs.spring.io/spring-security/reference/migration-7/index.html)
- [Jakarta EE 9 Migration](https://jakarta.ee/specifications/platform/9/)
- [JJWT 0.12.x Documentation](https://github.com/jwtk/jjwt#overview)

---

## ✅ 升级完成摘要

| 项目 | 状态 |
|------|------|
| 版本统一 | ✅ 完成 |
| Jakarta EE 迁移 | ✅ 完成 (9个文件) |
| Spring Security API 升级 | ✅ 完成 |
| JJWT 升级 | ✅ 完成 |
| bootJar 配置 | ✅ 完成 (6个库模块) |
| 编译验证 | ✅ 通过 |
| 单元测试 | ⏳ 待执行 |
| 运行时测试 | ⏳ 待执行 |

**升级完成时间**: 2024-12-29  
**升级人员**: AI Assistant  
**项目状态**: ✅ **编译成功** - BUILD SUCCESSFUL in 16s  
**下一步**: 运行时测试和功能验证

