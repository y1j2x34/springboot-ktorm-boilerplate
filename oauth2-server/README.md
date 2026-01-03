# OAuth2 Authorization Server Module

OAuth2 授权服务器模块，基于 Spring Authorization Server 实现。

## 功能特性

- ✅ OAuth2 客户端注册和管理
- ✅ 支持授权码流程（Authorization Code Flow）
- ✅ 支持客户端凭证流程（Client Credentials Flow）
- ✅ 支持刷新令牌（Refresh Token）
- ✅ 支持 PKCE（Proof Key for Code Exchange）
- ✅ OpenID Connect 1.0 支持
- ✅ 基于数据库的客户端存储
- ✅ 基于 JDBC 的授权信息存储

## 数据库表

模块需要以下数据库表：

1. **oauth2_client** - OAuth2 客户端注册表
2. **oauth2_authorization** - OAuth2 授权信息表（授权码、令牌等）
3. **oauth2_authorization_consent** - OAuth2 授权同意表

表结构定义在 `src/main/resources/schema.sql` 中。

## 配置

### 应用配置（application.yml）

```yaml
oauth2:
  authorization-server:
    issuer: http://localhost:8081/api
    authorization-endpoint: /oauth2/authorize
    token-endpoint: /oauth2/token
    token-revocation-endpoint: /oauth2/revoke
    token-introspection-endpoint: /oauth2/introspect
    jwk-set-endpoint: /oauth2/jwks
    oidc-user-info-endpoint: /userinfo
    oidc-client-registration-endpoint: /connect/register
```

### Security 配置

需要在应用层的 Security 配置中允许 OAuth2 授权服务器的端点：

```kotlin
.authorizeHttpRequests { authorize ->
    authorize.requestMatchers("/oauth2/**").permitAll()
    authorize.requestMatchers("/.well-known/**").permitAll()
    authorize.requestMatchers("/userinfo").permitAll()
    // ... 其他配置
}
```

## API 端点

### OAuth2 标准端点

- `GET /oauth2/authorize` - 授权端点
- `POST /oauth2/token` - Token 端点
- `POST /oauth2/revoke` - Token 撤销端点
- `POST /oauth2/introspect` - Token 内省端点
- `GET /oauth2/jwks` - JWK Set 端点
- `GET /.well-known/jwks.json` - JWK Set（OpenID Connect）
- `GET /.well-known/openid-configuration` - OpenID Connect 配置发现

### 管理 API

- `POST /api/admin/oauth2/clients` - 创建客户端
- `PUT /api/admin/oauth2/clients/{clientId}` - 更新客户端
- `DELETE /api/admin/oauth2/clients/{clientId}` - 删除客户端
- `GET /api/admin/oauth2/clients/{clientId}` - 获取客户端信息
- `GET /api/admin/oauth2/clients` - 获取所有客户端
- `GET /api/admin/oauth2/clients/enabled` - 获取所有启用的客户端

## 使用示例

### 创建 OAuth2 客户端

```bash
curl -X POST http://localhost:8081/api/admin/oauth2/clients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "clientId": "my-client",
    "clientSecret": "secret",
    "clientName": "My Client",
    "clientAuthenticationMethods": ["client_secret_basic"],
    "authorizationGrantTypes": ["authorization_code", "refresh_token"],
    "redirectUris": ["http://localhost:3000/callback"],
    "scopes": ["openid", "profile", "email"],
    "requireProofKey": false,
    "accessTokenValiditySeconds": 3600,
    "refreshTokenValiditySeconds": 604800,
    "enabled": true
  }'
```

### 授权码流程示例

1. **授权请求**：
```
GET /oauth2/authorize?client_id=my-client&response_type=code&redirect_uri=http://localhost:3000/callback&scope=openid profile email
```

2. **交换授权码**：
```bash
curl -X POST http://localhost:8081/api/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "my-client:secret" \
  -d "grant_type=authorization_code&code=<authorization-code>&redirect_uri=http://localhost:3000/callback"
```

3. **刷新令牌**：
```bash
curl -X POST http://localhost:8081/api/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "my-client:secret" \
  -d "grant_type=refresh_token&refresh_token=<refresh-token>"
```

## 依赖

- Spring Boot 3.2.4+
- Spring Security
- Spring Authorization Server
- Ktorm（数据库 ORM）
- MariaDB/MySQL

## 注意事项

1. 客户端密钥使用 BCrypt 加密存储
2. 授权信息存储在数据库中，支持集群部署
3. RSA 密钥对在启动时自动生成，生产环境建议使用固定的密钥对
4. 管理 API 需要 ADMIN 角色权限

