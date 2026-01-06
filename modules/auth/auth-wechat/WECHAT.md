# 微信登录集成指南

## 概述

`oauth-auth` 模块支持微信登录，基于 WxJava 库实现，支持以下三种登录方式：

| 登录类型 | 场景 | API 前缀 |
|---------|------|---------|
| `OPEN_PLATFORM` | PC 网站扫码登录 | `/public/wechat/open/{configId}` |
| `MP` | 微信内 H5 网页授权 | `/public/wechat/mp/{configId}` |
| `MINI_PROGRAM` | 微信小程序登录 | `/public/wechat/mini/{configId}` |

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < oauth-auth/src/main/resources/db/wechat_config.sql
```

### 2. 通过管理 API 添加微信配置

#### 添加微信开放平台配置（PC 扫码登录）

```bash
curl -X POST http://localhost:8080/api/admin/wechat/configs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "config_id": "wechat_open",
    "name": "微信扫码登录",
    "login_type": "OPEN_PLATFORM",
    "app_id": "your-open-app-id",
    "app_secret": "your-open-app-secret"
  }'
```

#### 添加微信公众号配置（微信内 H5 登录）

```bash
curl -X POST http://localhost:8080/api/admin/wechat/configs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "config_id": "wechat_mp",
    "name": "微信公众号登录",
    "login_type": "MP",
    "app_id": "your-mp-app-id",
    "app_secret": "your-mp-app-secret",
    "token": "your-token",
    "encoding_aes_key": "your-aes-key"
  }'
```

#### 添加微信小程序配置

```bash
curl -X POST http://localhost:8080/api/admin/wechat/configs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "config_id": "wechat_mini",
    "name": "微信小程序登录",
    "login_type": "MINI_PROGRAM",
    "app_id": "your-mini-app-id",
    "app_secret": "your-mini-app-secret"
  }'
```

## 登录流程

### 微信开放平台（PC 扫码登录）

```
┌─────────┐     ┌─────────────┐     ┌─────────────┐     ┌──────────┐
│  用户    │     │  前端页面    │     │  后端 API   │     │  微信服务  │
└────┬────┘     └──────┬──────┘     └──────┬──────┘     └────┬─────┘
     │                 │                    │                 │
     │  1. 点击登录     │                    │                 │
     │────────────────>│                    │                 │
     │                 │                    │                 │
     │                 │  2. 获取授权 URL    │                 │
     │                 │───────────────────>│                 │
     │                 │                    │                 │
     │                 │  3. 返回授权 URL    │                 │
     │                 │<───────────────────│                 │
     │                 │                    │                 │
     │  4. 重定向到微信  │                    │                 │
     │<────────────────│                    │                 │
     │                 │                    │                 │
     │  5. 扫码授权     │                    │                 │
     │─────────────────────────────────────────────────────>│
     │                 │                    │                 │
     │  6. 回调带 code  │                    │                 │
     │<─────────────────────────────────────────────────────│
     │                 │                    │                 │
     │                 │  7. 处理回调        │                 │
     │────────────────────────────────────>│                 │
     │                 │                    │                 │
     │                 │                    │  8. 换取用户信息 │
     │                 │                    │────────────────>│
     │                 │                    │                 │
     │                 │                    │  9. 返回用户信息 │
     │                 │                    │<────────────────│
     │                 │                    │                 │
     │                 │  10. 返回 JWT Token │                 │
     │<────────────────────────────────────│                 │
     │                 │                    │                 │
```

### API 端点

#### 1. 获取授权 URL

```http
GET /public/wechat/open/{configId}/auth-url?redirect_uri={回调地址}&state={状态参数}
```

响应：
```json
{
  "success": true,
  "data": {
    "auth_url": "https://open.weixin.qq.com/connect/qrconnect?appid=xxx&redirect_uri=xxx&response_type=code&scope=snsapi_login&state=xxx#wechat_redirect"
  }
}
```

#### 2. 直接重定向登录

```http
GET /public/wechat/open/{configId}/login?redirect_uri={回调地址}&state={状态参数}
```

自动重定向到微信授权页面。

#### 3. 处理回调

```http
GET /public/wechat/open/{configId}/callback?code={授权码}&state={状态参数}
```

响应：
```json
{
  "success": true,
  "message": "Wechat login successful",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "refresh_expires_in": 604800
  }
}
```

### 微信公众号（微信内 H5 登录）

与开放平台类似，但 API 前缀为 `/public/wechat/mp/{configId}`：

- `GET /public/wechat/mp/{configId}/auth-url` - 获取授权 URL
- `GET /public/wechat/mp/{configId}/login` - 直接重定向登录
- `GET /public/wechat/mp/{configId}/callback` - 处理回调

**额外参数：**
- `scope`: `snsapi_base`（静默授权）或 `snsapi_userinfo`（需用户确认，默认）

### 微信小程序

小程序使用 `wx.login()` 获取 code 后直接调用接口：

```http
POST /public/wechat/mini/{configId}/login
Content-Type: application/json

{
  "code": "小程序登录 code"
}
```

响应：
```json
{
  "success": true,
  "message": "Wechat login successful",
  "data": {
    "access_token": "...",
    "refresh_token": "...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "refresh_expires_in": 604800
  }
}
```

## 管理 API

### 获取所有配置

```http
GET /admin/wechat/configs
GET /admin/wechat/configs?includeDisabled=true
```

### 获取单个配置

```http
GET /admin/wechat/configs/{id}
```

### 按登录类型获取

```http
GET /admin/wechat/configs/type/{loginType}
```

`loginType`: `OPEN_PLATFORM`、`MP`、`MINI_PROGRAM`

### 创建配置

```http
POST /admin/wechat/configs
Content-Type: application/json

{
  "config_id": "wechat_open",
  "name": "微信扫码登录",
  "login_type": "OPEN_PLATFORM",
  "app_id": "your-app-id",
  "app_secret": "your-app-secret",
  "token": "可选",
  "encoding_aes_key": "可选",
  "status": 1,
  "sort_order": 1,
  "description": "描述"
}
```

### 更新配置

```http
PUT /admin/wechat/configs/{id}
Content-Type: application/json

{
  "name": "新名称",
  "status": 0
}
```

### 删除配置

```http
DELETE /admin/wechat/configs/{id}
```

### 刷新服务缓存

```http
POST /admin/wechat/configs/refresh
```

## 公开 API

### 获取所有启用的配置（供前端显示登录选项）

```http
GET /public/wechat/configs
```

响应：
```json
{
  "success": true,
  "data": [
    {
      "config_id": "wechat_open",
      "name": "微信扫码登录",
      "login_type": "OPEN_PLATFORM",
      "login_url": "/public/wechat/open/wechat_open/login"
    }
  ]
}
```

## 配置字段说明

| 字段 | 必填 | 说明 |
|-----|------|------|
| `config_id` | ✅ | 配置唯一标识 |
| `name` | ✅ | 显示名称 |
| `login_type` | ✅ | 登录类型：`OPEN_PLATFORM`、`MP`、`MINI_PROGRAM` |
| `app_id` | ✅ | 微信 AppID |
| `app_secret` | ✅ | 微信 AppSecret |
| `token` | ❌ | 公众号消息验证 Token |
| `encoding_aes_key` | ❌ | 消息加解密密钥 |
| `status` | ❌ | 状态：1-启用，0-禁用（默认 1） |
| `sort_order` | ❌ | 排序顺序（默认 0） |
| `description` | ❌ | 描述 |

## 用户映射

微信用户首次登录时，系统会自动创建用户账户：

- **用户名格式**: `wx_{unionid}` 或 `wx_{openid}`（优先使用 unionid）
- **邮箱**: `{username}@wechat.local`
- **密码**: 随机生成（OAuth 用户不使用密码登录）

## 微信开发者配置

### 微信开放平台

1. 登录 [微信开放平台](https://open.weixin.qq.com/)
2. 创建网站应用
3. 配置授权回调域名（如 `your-domain.com`）
4. 获取 AppID 和 AppSecret

### 微信公众号

1. 登录 [微信公众平台](https://mp.weixin.qq.com/)
2. 开发 → 基本配置 → 获取 AppID 和 AppSecret
3. 设置 → 公众号设置 → 功能设置 → 网页授权域名

### 微信小程序

1. 登录 [微信小程序后台](https://mp.weixin.qq.com/)
2. 开发 → 开发设置 → 获取 AppID 和 AppSecret

## 注意事项

1. **回调域名**：必须在微信开发者后台配置授权回调域名
2. **HTTPS**：生产环境必须使用 HTTPS
3. **unionid**：如果需要跨应用识别用户，需要将应用绑定到同一个微信开放平台账号
4. **Token 安全**：AppSecret 和 Token 请妥善保管，不要泄露

## 故障排除

### 问题：授权后显示"redirect_uri 参数错误"

**解决方案**：检查微信开发者后台配置的授权回调域名是否正确。

### 问题：获取用户信息失败

**解决方案**：
1. 检查 AppID 和 AppSecret 是否正确
2. 检查微信服务是否正常
3. 查看日志获取详细错误信息

### 问题：配置修改后不生效

**解决方案**：调用 `POST /admin/wechat/configs/refresh` 刷新服务缓存。

