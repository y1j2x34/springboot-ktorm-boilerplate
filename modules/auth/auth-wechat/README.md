# 微信登录认证模块

基于 WxJava 的微信登录认证模块，支持微信开放平台、微信公众号和微信小程序登录。

## 功能特性

- ✅ 微信开放平台（PC 扫码登录）
- ✅ 微信公众号（微信内 H5 网页授权）
- ✅ 微信小程序登录
- ✅ 从数据库读取配置
- ✅ 运行时动态管理配置
- ✅ 统一返回 JWT Token

## 快速开始

### 1. 添加依赖

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    implementation(project(":wechat-auth"))
    // ... 其他依赖
}
```

### 2. 初始化数据库

```bash
mysql -u root -p < wechat-auth/src/main/resources/db/wechat_config.sql
```

### 3. 通过管理 API 添加微信配置

```bash
curl -X POST http://localhost:8080/api/admin/wechat/configs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "config_id": "wechat_open",
    "name": "微信扫码登录",
    "login_type": "OPEN_PLATFORM",
    "app_id": "your-app-id",
    "app_secret": "your-app-secret"
  }'
```

## API 端点

### 微信开放平台（PC 扫码登录）

- `GET /public/wechat/open/{configId}/auth-url` - 获取授权 URL
- `GET /public/wechat/open/{configId}/login` - 重定向登录
- `GET /public/wechat/open/{configId}/callback` - 处理回调

### 微信公众号（微信内 H5）

- `GET /public/wechat/mp/{configId}/auth-url` - 获取授权 URL
- `GET /public/wechat/mp/{configId}/login` - 重定向登录
- `GET /public/wechat/mp/{configId}/callback` - 处理回调

### 微信小程序

- `POST /public/wechat/mini/{configId}/login` - 小程序登录

## 管理 API

- `GET /admin/wechat/configs` - 获取所有配置
- `POST /admin/wechat/configs` - 创建配置
- `PUT /admin/wechat/configs/{id}` - 更新配置
- `DELETE /admin/wechat/configs/{id}` - 删除配置
- `POST /admin/wechat/configs/refresh` - 刷新服务缓存

## 相关文档

- [微信登录集成指南](./WECHAT.md)
- [数据库脚本](./src/main/resources/db/wechat_config.sql)

