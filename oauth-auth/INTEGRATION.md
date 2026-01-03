# OAuth2/OIDC 模块集成指南

## 概述

`oauth-auth` 模块提供了基于 OAuth 2.0 和 OpenID Connect (OIDC) 的认证支持，采用 Authorization Code Flow（授权码流程）。该模块可以与现有的 JWT 认证共存。

**重要更新：OAuth2 提供商配置现在从数据库读取，支持运行时动态管理。**

## 功能特性

- ✅ 支持 OAuth 2.0 Authorization Code Flow
- ✅ 支持 OpenID Connect (OIDC)
- ✅ 自动发现 OIDC 配置（通过 issuer-uri）
- ✅ 手动配置 OAuth2 端点
- ✅ 自动创建用户（如果用户不存在）
- ✅ 与 JWT 认证共存
- ✅ OAuth2 登录后自动生成 JWT Token
- ✅ 从数据库读取提供商配置
- ✅ 运行时动态管理提供商
- ✅ 管理 API 支持 CRUD 操作

## 快速开始

### 1. 初始化数据库

执行 SQL 脚本创建 OAuth2 提供商表：

```sql
-- 执行 oauth-auth/src/main/resources/db/oauth2_provider.sql
```

或者手动创建：

```sql
CREATE TABLE IF NOT EXISTS `oauth2_provider` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `registration_id` VARCHAR(50) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `client_id` VARCHAR(255) NOT NULL,
    `client_secret` VARCHAR(500) NOT NULL,
    `authorization_uri` VARCHAR(500) NULL,
    `token_uri` VARCHAR(500) NULL,
    `user_info_uri` VARCHAR(500) NULL,
    `jwk_set_uri` VARCHAR(500) NULL,
    `issuer_uri` VARCHAR(500) NULL,
    `redirect_uri` VARCHAR(500) NULL,
    `scopes` VARCHAR(500) NOT NULL DEFAULT 'openid,profile,email',
    `user_name_attribute_name` VARCHAR(100) NOT NULL DEFAULT 'sub',
    `status` INT NOT NULL DEFAULT 1,
    `sort_order` INT NOT NULL DEFAULT 0,
    `description` VARCHAR(500) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 2. 添加依赖

确保在 `app/build.gradle.kts` 中添加 `oauth-auth` 模块依赖：

```kotlin
dependencies {
    implementation(project(":oauth-auth"))
    // ... 其他依赖
}
```

### 3. 启用 OAuth2

在应用配置文件中添加（例如 `application-dev.yml`）：

```yaml
oauth2:
  enabled: true
```

### 4. 更新 Security 配置

在 `AppSecurityConfiguration` 中添加 OAuth2 登录配置：

```kotlin
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class AppSecurityConfiguration {
    
    @Autowired
    lateinit var jwtRequestFilter: JwtRequestFilter
    
    @Autowired
    lateinit var oauth2SuccessHandler: OAuth2SuccessHandler
    
    @Autowired
    lateinit var oauth2FailureHandler: OAuth2FailureHandler
    
    @Bean
    fun securityFilterChain(http: HttpSecurity, daoAuthenticationProvider: DaoAuthenticationProvider): DefaultSecurityFilterChain =
        http
            .cors { it.disable() }
            .csrf { it.disable() }
            .exceptionHandling { it.authenticationEntryPoint(unauthorizedHandler) }
            .authorizeHttpRequests { authorize ->
                authorize.requestMatchers(HttpMethod.POST, "/public/**").permitAll()
                authorize.requestMatchers(HttpMethod.GET, "/public/**").permitAll()
                authorize.requestMatchers(HttpMethod.PUT, "/public/**").permitAll()
                authorize.requestMatchers("/login/oauth2/**").permitAll()
                authorize.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                authorize.anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .successHandler(oauth2SuccessHandler)
                    .failureHandler(oauth2FailureHandler)
            }
            .authenticationProvider(daoAuthenticationProvider)
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
}
```

### 5. 通过管理 API 添加 OAuth2 提供商

使用管理 API 添加 OAuth2 提供商配置：

```bash
# 添加 Google OAuth2 提供商
curl -X POST http://localhost:8080/api/admin/oauth2/providers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "registration_id": "google",
    "name": "Google",
    "client_id": "your-google-client-id",
    "client_secret": "your-google-client-secret",
    "issuer_uri": "https://accounts.google.com",
    "scopes": "openid,profile,email",
    "user_name_attribute_name": "sub"
  }'
```

## 管理 API 说明

### 获取所有提供商

```http
GET /admin/oauth2/providers
GET /admin/oauth2/providers?includeDisabled=true
```

### 获取单个提供商

```http
GET /admin/oauth2/providers/{id}
```

### 创建提供商

```http
POST /admin/oauth2/providers
Content-Type: application/json

{
  "registration_id": "google",
  "name": "Google",
  "client_id": "your-client-id",
  "client_secret": "your-client-secret",
  "issuer_uri": "https://accounts.google.com",
  "scopes": "openid,profile,email",
  "user_name_attribute_name": "sub",
  "status": 1,
  "sort_order": 1,
  "description": "Google OAuth2 登录"
}
```

### 更新提供商

```http
PUT /admin/oauth2/providers/{id}
Content-Type: application/json

{
  "name": "Google OAuth2",
  "status": 0
}
```

### 删除提供商（逻辑删除）

```http
DELETE /admin/oauth2/providers/{id}
```

### 刷新配置缓存

```http
POST /admin/oauth2/providers/refresh
```

### 公开 API（获取登录选项）

```http
GET /public/oauth2/providers
```

响应：
```json
{
  "success": true,
  "data": [
    {
      "registration_id": "google",
      "name": "Google",
      "login_url": "/login/oauth2/authorization/google"
    }
  ]
}
```

## 配置字段说明

| 字段 | 必填 | 说明 |
|-----|------|------|
| `registration_id` | ✅ | 唯一标识，如 `google`、`github` |
| `name` | ✅ | 显示名称 |
| `client_id` | ✅ | OAuth2 客户端 ID |
| `client_secret` | ✅ | OAuth2 客户端密钥 |
| `issuer_uri` | ⚠️ | OIDC 发行者 URI（提供此字段可自动发现其他端点） |
| `authorization_uri` | ⚠️ | 授权端点 URI（如果没有 issuer_uri 则必填） |
| `token_uri` | ⚠️ | Token 端点 URI（如果没有 issuer_uri 则必填） |
| `user_info_uri` | ❌ | 用户信息端点 URI |
| `jwk_set_uri` | ❌ | JWK Set URI（用于验证 ID Token） |
| `redirect_uri` | ❌ | 重定向 URI，默认 `{baseUrl}/login/oauth2/code/{registrationId}` |
| `scopes` | ❌ | 权限范围，逗号分隔，默认 `openid,profile,email` |
| `user_name_attribute_name` | ❌ | 用户名属性名，默认 `sub` |
| `status` | ❌ | 状态：1-启用，0-禁用，默认 1 |
| `sort_order` | ❌ | 排序顺序，默认 0 |
| `description` | ❌ | 描述 |

## OAuth2 登录端点

### 启动 OAuth2 登录

```
GET /login/oauth2/authorization/{registrationId}
```

例如：
- `/login/oauth2/authorization/google` - 使用 Google 登录
- `/login/oauth2/authorization/github` - 使用 GitHub 登录

### 登录成功响应

OAuth2 登录成功后，会重定向到：

```
GET /public/oauth2/login/success
```

响应示例：

```json
{
  "success": true,
  "message": "OAuth2 login successful",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "refresh_expires_in": 604800
  }
}
```

### 获取当前用户信息

```
GET /public/oauth2/user
```

需要 Bearer Token 认证。

## 常见提供商配置示例

### Google (OIDC)

```json
{
  "registration_id": "google",
  "name": "Google",
  "client_id": "your-client-id",
  "client_secret": "your-client-secret",
  "issuer_uri": "https://accounts.google.com",
  "scopes": "openid,profile,email",
  "user_name_attribute_name": "sub"
}
```

### GitHub (OAuth2)

```json
{
  "registration_id": "github",
  "name": "GitHub",
  "client_id": "your-client-id",
  "client_secret": "your-client-secret",
  "authorization_uri": "https://github.com/login/oauth/authorize",
  "token_uri": "https://github.com/login/oauth/access_token",
  "user_info_uri": "https://api.github.com/user",
  "scopes": "read:user,user:email",
  "user_name_attribute_name": "login"
}
```

### Keycloak (OIDC)

```json
{
  "registration_id": "keycloak",
  "name": "Keycloak",
  "client_id": "your-client-id",
  "client_secret": "your-client-secret",
  "issuer_uri": "https://your-keycloak-server.com/realms/your-realm",
  "scopes": "openid,profile,email",
  "user_name_attribute_name": "preferred_username"
}
```

## 用户创建

当用户通过 OAuth2 首次登录时，系统会自动创建用户账户。用户名从 OAuth2 用户信息中提取，优先级：

1. `user_name_attribute_name` 指定的属性
2. `email` 属性
3. `preferred_username` 属性
4. OAuth2User 的 `name` 属性

如果用户已存在（通过用户名匹配），则直接使用现有账户。

## 与 JWT 认证共存

OAuth2 和 JWT 认证可以完全共存：

- **JWT 登录**: `POST /public/auth/login` - 使用用户名密码登录
- **OAuth2 登录**: `GET /login/oauth2/authorization/{provider}` - 使用 OAuth2 提供商登录

两种方式登录后都会返回 JWT Token，后续 API 调用使用相同的 Bearer Token 认证方式。

## 缓存机制

OAuth2 提供商配置会在首次访问时从数据库加载并缓存。当通过管理 API 修改配置后，缓存会自动刷新。

如果直接修改数据库，需要手动调用刷新接口：

```bash
curl -X POST http://localhost:8080/api/admin/oauth2/providers/refresh \
  -H "Authorization: Bearer <admin-token>"
```

## 注意事项

1. **重定向 URI 配置**: 在 OAuth2 提供商处配置的重定向 URI 必须是：
   ```
   {baseUrl}/login/oauth2/code/{registrationId}
   ```
   例如：`http://localhost:8080/api/login/oauth2/code/google`

2. **密码字段**: OAuth2 用户创建时会生成一个随机密码（不会被使用），因为 `CreateUserDto` 要求密码字段非空。

3. **用户名字段**: 确保配置的 `user_name_attribute_name` 在 OAuth2 用户信息中存在，否则登录会失败。

4. **作用域**: 根据提供商的要求配置正确的 `scopes`，例如 Google 需要 `openid`、`profile`、`email`。

5. **管理 API 权限**: 管理 API 需要 `ROLE_ADMIN` 权限。

## 故障排除

### 问题：OAuth2 登录后无法获取用户信息

**解决方案**: 检查 `user_name_attribute_name` 配置是否正确，确保该属性在 OAuth2 用户信息中存在。

### 问题：重定向 URI 不匹配

**解决方案**: 确保在 OAuth2 提供商处配置的重定向 URI 与 Spring Security 的默认格式一致。

### 问题：用户创建失败

**解决方案**: 检查数据库连接和用户表结构，确保用户名和邮箱字段符合要求。

### 问题：配置修改后不生效

**解决方案**: 调用 `POST /admin/oauth2/providers/refresh` 刷新配置缓存。
