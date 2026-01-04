# OAuth2/OIDC 认证模块

基于 OAuth 2.0 和 OpenID Connect (OIDC) 的认证模块，采用 Authorization Code Flow（授权码流程）。

## 功能特性

- ✅ 支持 OAuth 2.0 Authorization Code Flow
- ✅ 支持 OpenID Connect (OIDC)
- ✅ 自动发现 OIDC 配置（通过 issuer-uri）
- ✅ 手动配置 OAuth2 端点
- ✅ 自动创建用户（如果用户不存在）
- ✅ 与 JWT 认证共存
- ✅ OAuth2 登录后自动生成 JWT Token
- ✅ **从数据库读取提供商配置**
- ✅ **运行时动态管理提供商**
- ✅ **管理 API 支持 CRUD 操作**

## 模块结构

```
oauth-auth/
├── src/main/kotlin/com/vgerbot/oauth/
│   ├── OAuth2ConfigurationProperties.kt     # 配置属性类
│   ├── OAuth2Configuration.kt               # OAuth2 配置类
│   ├── OAuth2ClientRegistrationConfig.kt    # 动态客户端注册配置
│   ├── OAuth2SecurityConfig.kt              # Security 配置辅助类
│   ├── OAuth2Controller.kt                  # OAuth2 认证控制器
│   ├── OAuth2ProviderController.kt          # 提供商管理 API
│   ├── OAuth2SuccessHandler.kt              # 成功处理器
│   ├── OAuth2FailureHandler.kt              # 失败处理器
│   ├── entity/
│   │   └── OAuth2Provider.kt                # 提供商实体
│   ├── dao/
│   │   └── OAuth2ProviderDao.kt             # 提供商 DAO
│   ├── dto/
│   │   └── OAuth2ProviderDto.kt             # 提供商 DTO
│   └── service/
│       ├── OAuth2ProviderService.kt         # 提供商服务
│       └── CustomOAuth2UserService.kt       # 自定义用户服务
└── src/main/resources/
    ├── application-oauth2.yml.example       # 配置示例
    └── db/
        └── oauth2_provider.sql              # 数据库初始化脚本
```

## 快速开始

### 1. 初始化数据库

执行 SQL 脚本创建 OAuth2 提供商表：

```bash
mysql -u root -p < oauth-auth/src/main/resources/db/oauth2_provider.sql
```

### 2. 添加依赖

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    implementation(project(":oauth-auth"))
}
```

### 3. 启用 OAuth2

在应用配置文件中添加：

```yaml
oauth2:
  enabled: true
```

### 4. 通过 API 添加 OAuth2 提供商

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

### 5. 更新 Security 配置

在 `AppSecurityConfiguration` 中添加 OAuth2 登录配置：

```kotlin
.oauth2Login { oauth2 ->
    oauth2
        .successHandler(oauth2SuccessHandler)
        .failureHandler(oauth2FailureHandler)
}
```

## 管理 API

### 获取所有提供商
```
GET /admin/oauth2/providers
GET /admin/oauth2/providers?includeDisabled=true
```

### 获取单个提供商
```
GET /admin/oauth2/providers/{id}
```

### 创建提供商
```
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
  "sort_order": 1
}
```

### 更新提供商
```
PUT /admin/oauth2/providers/{id}
Content-Type: application/json

{
  "name": "Google OAuth2",
  "status": 0
}
```

### 删除提供商
```
DELETE /admin/oauth2/providers/{id}
```

### 刷新配置缓存
```
POST /admin/oauth2/providers/refresh
```

### 公开 API（获取登录选项）
```
GET /public/oauth2/providers
```

响应示例：
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

## OAuth2 登录 API

- `GET /login/oauth2/authorization/{provider}` - 启动 OAuth2 登录
- `GET /public/oauth2/login/success` - 登录成功回调
- `GET /public/oauth2/login/failure` - 登录失败回调
- `GET /public/oauth2/user` - 获取当前用户信息

## 配置示例

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

## 注意事项

1. **重定向 URI**: 在 OAuth2 提供商处配置的重定向 URI 必须是 `{baseUrl}/login/oauth2/code/{registrationId}`
2. **密码字段**: OAuth2 用户创建时会生成随机密码（不会被使用）
3. **用户名属性**: 确保配置的 `user_name_attribute_name` 在用户信息中存在
4. **管理 API 权限**: 管理 API 需要 `ROLE_ADMIN` 权限

## 相关文档

- [集成指南](./INTEGRATION.md)
- [配置示例](./src/main/resources/application-oauth2.yml.example)
- [OAuth2 数据库脚本](./src/main/resources/db/oauth2_provider.sql)

**注意**：微信登录功能已独立到 `wechat-auth` 模块，如需使用请单独引入该模块。
