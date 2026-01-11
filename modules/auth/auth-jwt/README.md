# JWT 认证模块 - 客户端集成指南

## 概述

本模块提供基于 JWT 的用户认证功能，支持两种登录方式：

1. **RSA 加密登录（推荐）**：使用临时 RSA 密钥对加密密码，确保密码传输安全
2. **传统登录（向后兼容）**：直接传输明文密码

## 核心特性

- ✅ **零明文传输**：密码全程加密传输
- ✅ **会话隔离**：每个用户会话独立密钥对
- ✅ **用后即焚**：私钥使用后立即删除
- ✅ **自动过期**：密钥 5 分钟自动过期
- ✅ **防御重放**：每个密钥一次性使用
- ✅ **向后兼容**：支持传统明文密码登录

## API 端点

### 基础 URL

所有 API 端点基于配置的 `context-path`（默认为 `/api`）：

- 开发环境：`http://localhost:8081/api`
- 生产环境：根据实际配置

### 端点列表

| 方法 | 路径 | 说明 | 认证要求 |
|------|------|------|----------|
| GET | `/public/auth/public-key` | 获取 RSA 公钥 | 否 |
| POST | `/public/auth/login` | 用户登录 | 否 |
| POST | `/public/auth/register` | 用户注册 | 否 |
| POST | `/public/auth/refresh` | 刷新 Token | 否 |
| POST | `/public/auth/logout` | 用户登出 | 是 |
| GET | `/public/auth/me` | 获取当前用户信息 | 是 |

---

## 1. RSA 加密登录（推荐）

### 1.1 工作流程

```
1. 前端生成或获取 Session ID
2. 调用 GET /public/auth/public-key 获取公钥
3. 用户输入密码，前端使用公钥加密
4. 调用 POST /public/auth/login 提交加密密码和 keyId
5. 后端解密密码并验证，成功后返回 JWT Token
6. 前端保存 Token，用于后续请求认证
```

### 1.2 获取公钥

**请求：**

```http
GET /api/public/auth/public-key
Headers:
  X-Session-Id: <可选，会话ID>
```

**响应：**

```json
{
  "code": 200,
  "message": "公钥获取成功",
  "data": {
    "key_id": "session_abc123_1641895200000",
    "public_key": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...\n-----END PUBLIC KEY-----",
    "expires_at": 1641895500000,
    "algorithm": "RSA"
  }
}
```

**字段说明：**

- `key_id`: 密钥ID，用于后续登录请求
- `public_key`: RSA 公钥（PEM 格式），用于加密密码
- `expires_at`: 密钥过期时间（Unix 时间戳，毫秒）
- `algorithm`: 加密算法，固定为 "RSA"

### 1.3 加密登录

**请求：**

```http
POST /api/public/auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "encryptedPassword": "base64_encoded_encrypted_password",
  "keyId": "session_abc123_1641895200000"
}
```

**响应：**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "refresh_expires_in": 86400
  }
}
```

---

## 2. 传统登录（向后兼容）

如果不需要加密传输，可以直接使用明文密码登录：

**请求：**

```http
POST /api/public/auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "plaintext_password"
}
```

**响应：** 同加密登录

---

## 3. 用户注册

**请求：**

```http
POST /api/public/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "email": "user@example.com",
  "password": "plaintext_password",
  "phoneNumber": "13800138000"  // 可选
}
```

**响应：**

```json
{
  "code": 201,
  "message": "用户注册成功",
  "data": null
}
```

**错误响应：**

```json
{
  "code": 409,
  "message": "用户已存在",
  "data": null
}
```

---

## 4. Token 刷新

当 Access Token 过期时，使用 Refresh Token 获取新的 Token：

**请求：**

```http
POST /api/public/auth/refresh
Content-Type: application/json

{
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**响应：** 同登录响应

---

## 5. 用户登出

**请求：**

```http
POST /api/public/auth/logout
Headers:
  Authorization: Bearer <access_token>
```

**响应：**

```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

---

## 6. 获取当前用户信息

**请求：**

```http
GET /api/public/auth/me
Headers:
  Authorization: Bearer <access_token>
```

**响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "user@example.com",
    "authorities": ["ROLE_USER"]
  }
}
```

---

## 7. 错误处理

### 7.1 错误响应格式

所有错误响应遵循统一格式：

```json
{
  "code": <HTTP状态码>,
  "message": "<错误消息>",
  "data": null
}
```

### 7.2 错误码说明

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 400 | 请求格式错误 | 检查请求参数 |
| 401 | 认证失败 | 检查用户名密码或 Token |
| 409 | 资源冲突 | 用户已存在 |
| 500 | 服务器错误 | 稍后重试 |

### 7.3 登录错误消息

登录接口可能返回以下错误消息（包含错误类型前缀）：

- `KEY_EXPIRED`: 安全会话已过期，请重新获取公钥
- `KEY_NOT_FOUND`: 安全验证失败，请刷新页面重试
- `INVALID_CREDENTIALS`: 用户名或密码错误

### 7.4 自动重试机制

当遇到 `KEY_EXPIRED` 或 `KEY_NOT_FOUND` 错误时，前端应：

1. 清除当前公钥缓存
2. 重新获取公钥
3. 使用新公钥重新加密密码
4. 自动重试登录请求
5. 最多重试 1 次，避免无限循环

---

## 8. 前端实现示例

### 8.1 JavaScript/TypeScript 实现

#### 安装依赖

```bash
npm install jsencrypt
# 或
yarn add jsencrypt
```

#### 认证服务类

```typescript
import JSEncrypt from 'jsencrypt';

interface PublicKeyInfo {
  key_id: string;
  public_key: string;
  expires_at: number;
  algorithm: string;
}

interface LoginResponse {
  access_token: string;
  refresh_token: string;
  token_type: string;
  expires_in: number;
  refresh_expires_in: number;
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

class AuthService {
  private baseUrl: string;
  private sessionId: string;
  private currentKey: PublicKeyInfo | null = null;
  private refreshTimer: NodeJS.Timeout | null = null;

  constructor(baseUrl: string = '/api') {
    this.baseUrl = baseUrl;
    this.sessionId = this.getOrCreateSessionId();
  }

  /**
   * 获取或创建 Session ID
   */
  private getOrCreateSessionId(): string {
    const storageKey = 'auth_session_id';
    let sessionId = localStorage.getItem(storageKey);
    
    if (!sessionId) {
      sessionId = this.generateSessionId();
      localStorage.setItem(storageKey, sessionId);
    }
    
    return sessionId;
  }

  /**
   * 生成 Session ID
   */
  private generateSessionId(): string {
    return `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * 获取公钥
   */
  async getPublicKey(forceRefresh: boolean = false): Promise<PublicKeyInfo> {
    // 如果已有有效公钥且不强制刷新，直接返回
    if (!forceRefresh && this.currentKey && Date.now() < this.currentKey.expires_at) {
      return this.currentKey;
    }

    try {
      const response = await fetch(`${this.baseUrl}/public/auth/public-key`, {
        method: 'GET',
        headers: {
          'X-Session-Id': this.sessionId,
        },
      });

      if (!response.ok) {
        throw new Error('获取公钥失败');
      }

      const result: ApiResponse<PublicKeyInfo> = await response.json();
      
      if (result.code !== 200) {
        throw new Error(result.message || '获取公钥失败');
      }

      this.currentKey = result.data;
      this.scheduleAutoRefresh();
      
      return result.data;
    } catch (error) {
      console.error('获取公钥失败:', error);
      throw error;
    }
  }

  /**
   * 使用 RSA 公钥加密密码
   */
  private encryptPassword(password: string, publicKey: string): string {
    const encrypt = new JSEncrypt();
    encrypt.setPublicKey(publicKey);
    const encrypted = encrypt.encrypt(password);
    
    if (!encrypted) {
      throw new Error('密码加密失败');
    }
    
    return encrypted;
  }

  /**
   * RSA 加密登录
   */
  async loginEncrypted(username: string, password: string): Promise<LoginResponse> {
    let retryCount = 0;
    const maxRetries = 1;

    while (retryCount <= maxRetries) {
      try {
        // 获取公钥
        const keyInfo = await this.getPublicKey(retryCount > 0);
        
        // 加密密码
        const encryptedPassword = this.encryptPassword(password, keyInfo.public_key);
        
        // 发送登录请求
        const response = await fetch(`${this.baseUrl}/public/auth/login`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            username,
            encryptedPassword,
            keyId: keyInfo.key_id,
          }),
        });

        const result: ApiResponse<LoginResponse> = await response.json();

        if (result.code === 200) {
          // 登录成功，清除公钥缓存
          this.currentKey = null;
          this.clearAutoRefresh();
          
          // 保存 Token
          this.saveTokens(result.data);
          
          return result.data;
        }

        // 处理密钥相关错误
        if (result.message.includes('KEY_EXPIRED') || result.message.includes('KEY_NOT_FOUND')) {
          if (retryCount < maxRetries) {
            retryCount++;
            this.currentKey = null; // 清除无效公钥
            console.log('密钥过期，正在重试...');
            continue;
          }
        }

        throw new Error(result.message || '登录失败');
      } catch (error) {
        if (retryCount >= maxRetries) {
          throw error;
        }
        retryCount++;
      }
    }

    throw new Error('登录失败，请稍后重试');
  }

  /**
   * 传统登录（明文密码）
   */
  async loginPlain(username: string, password: string): Promise<LoginResponse> {
    const response = await fetch(`${this.baseUrl}/public/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username,
        password,
      }),
    });

    const result: ApiResponse<LoginResponse> = await response.json();

    if (result.code !== 200) {
      throw new Error(result.message || '登录失败');
    }

    this.saveTokens(result.data);
    return result.data;
  }

  /**
   * 用户注册
   */
  async register(username: string, email: string, password: string, phoneNumber?: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/public/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username,
        email,
        password,
        phoneNumber,
      }),
    });

    const result: ApiResponse<null> = await response.json();

    if (result.code !== 201) {
      throw new Error(result.message || '注册失败');
    }
  }

  /**
   * 刷新 Token
   */
  async refreshToken(): Promise<LoginResponse> {
    const refreshToken = localStorage.getItem('refresh_token');
    
    if (!refreshToken) {
      throw new Error('未找到刷新令牌');
    }

    const response = await fetch(`${this.baseUrl}/public/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        refresh_token: refreshToken,
      }),
    });

    const result: ApiResponse<LoginResponse> = await response.json();

    if (result.code !== 200) {
      // Token 刷新失败，清除本地 Token
      this.clearTokens();
      throw new Error(result.message || 'Token 刷新失败');
    }

    this.saveTokens(result.data);
    return result.data;
  }

  /**
   * 用户登出
   */
  async logout(): Promise<void> {
    const accessToken = localStorage.getItem('access_token');
    
    if (accessToken) {
      try {
        await fetch(`${this.baseUrl}/public/auth/logout`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
          },
        });
      } catch (error) {
        console.error('登出请求失败:', error);
      }
    }

    this.clearTokens();
    this.currentKey = null;
    this.clearAutoRefresh();
  }

  /**
   * 获取当前用户信息
   */
  async getCurrentUser(): Promise<any> {
    const accessToken = localStorage.getItem('access_token');
    
    if (!accessToken) {
      throw new Error('未登录');
    }

    const response = await fetch(`${this.baseUrl}/public/auth/me`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
      },
    });

    const result: ApiResponse<any> = await response.json();

    if (result.code !== 200) {
      throw new Error(result.message || '获取用户信息失败');
    }

    return result.data;
  }

  /**
   * 保存 Token
   */
  private saveTokens(tokens: LoginResponse): void {
    localStorage.setItem('access_token', tokens.access_token);
    localStorage.setItem('refresh_token', tokens.refresh_token);
    localStorage.setItem('token_expires_at', String(Date.now() + tokens.expires_in * 1000));
  }

  /**
   * 清除 Token
   */
  private clearTokens(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('token_expires_at');
  }

  /**
   * 检查 Token 是否过期
   */
  isTokenExpired(): boolean {
    const expiresAt = localStorage.getItem('token_expires_at');
    if (!expiresAt) {
      return true;
    }
    return Date.now() >= parseInt(expiresAt);
  }

  /**
   * 获取 Access Token
   */
  getAccessToken(): string | null {
    return localStorage.getItem('access_token');
  }

  /**
   * 计划自动刷新公钥（在过期前 30 秒）
   */
  private scheduleAutoRefresh(): void {
    if (!this.currentKey) {
      return;
    }

    this.clearAutoRefresh();

    const timeToExpire = this.currentKey.expires_at - Date.now();
    const refreshTime = Math.max(timeToExpire - 30000, 5000); // 至少 5 秒后刷新

    this.refreshTimer = setTimeout(() => {
      this.getPublicKey(true).catch(console.error);
    }, refreshTime);
  }

  /**
   * 清除自动刷新定时器
   */
  private clearAutoRefresh(): void {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
      this.refreshTimer = null;
    }
  }
}

export default AuthService;
```

#### 使用示例

```typescript
// 创建认证服务实例
const authService = new AuthService('http://localhost:8081/api');

// RSA 加密登录（推荐）
try {
  const tokens = await authService.loginEncrypted('user@example.com', 'password123');
  console.log('登录成功:', tokens);
} catch (error) {
  console.error('登录失败:', error);
}

// 传统登录（向后兼容）
try {
  const tokens = await authService.loginPlain('user@example.com', 'password123');
  console.log('登录成功:', tokens);
} catch (error) {
  console.error('登录失败:', error);
}

// 用户注册
try {
  await authService.register('newuser', 'user@example.com', 'password123', '13800138000');
  console.log('注册成功');
} catch (error) {
  console.error('注册失败:', error);
}

// 刷新 Token
try {
  const newTokens = await authService.refreshToken();
  console.log('Token 刷新成功:', newTokens);
} catch (error) {
  console.error('Token 刷新失败:', error);
}

// 用户登出
await authService.logout();

// 获取当前用户信息
try {
  const user = await authService.getCurrentUser();
  console.log('当前用户:', user);
} catch (error) {
  console.error('获取用户信息失败:', error);
}
```

### 8.2 React Hook 示例

```typescript
import { useState, useEffect } from 'react';
import AuthService from './AuthService';

const authService = new AuthService();

export function useAuth() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState(null);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    try {
      if (!authService.isTokenExpired()) {
        const userInfo = await authService.getCurrentUser();
        setUser(userInfo);
        setIsAuthenticated(true);
      } else {
        // 尝试刷新 Token
        try {
          await authService.refreshToken();
          const userInfo = await authService.getCurrentUser();
          setUser(userInfo);
          setIsAuthenticated(true);
        } catch {
          setIsAuthenticated(false);
        }
      }
    } catch {
      setIsAuthenticated(false);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (username: string, password: string) => {
    setIsLoading(true);
    try {
      const tokens = await authService.loginEncrypted(username, password);
      const userInfo = await authService.getCurrentUser();
      setUser(userInfo);
      setIsAuthenticated(true);
      return tokens;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    setIsLoading(true);
    try {
      await authService.logout();
      setUser(null);
      setIsAuthenticated(false);
    } finally {
      setIsLoading(false);
    }
  };

  return {
    isAuthenticated,
    isLoading,
    user,
    login,
    logout,
  };
}
```

---

## 9. 安全建议

### 9.1 密码传输

- ✅ **推荐使用 RSA 加密登录**，确保密码在传输过程中不被窃取
- ❌ **避免使用传统明文密码登录**，除非在 HTTPS 环境下

### 9.2 Token 存储

- ✅ 使用 `localStorage` 或 `sessionStorage` 存储 Token
- ✅ 定期检查 Token 过期时间，提前刷新
- ❌ 不要在 Cookie 中存储敏感信息（除非设置了 HttpOnly 和 Secure）

### 9.3 会话管理

- ✅ 使用唯一的 Session ID 标识用户会话
- ✅ 在用户登出时清除所有本地存储的 Token
- ✅ 实现 Token 自动刷新机制

### 9.4 错误处理

- ✅ 实现友好的错误提示
- ✅ 对密钥过期错误实现自动重试
- ✅ 避免在错误消息中暴露敏感信息

### 9.5 HTTPS

- ✅ **生产环境必须使用 HTTPS**
- ✅ 确保所有 API 请求通过 HTTPS 传输

---

## 10. 常见问题

### Q1: 公钥过期了怎么办？

A: 前端会自动检测公钥过期时间，在过期前 30 秒自动刷新。如果登录时遇到 `KEY_EXPIRED` 错误，前端会自动重新获取公钥并重试登录。

### Q2: 可以同时使用加密登录和传统登录吗？

A: 可以。系统同时支持两种登录方式，但推荐使用加密登录以确保安全。

### Q3: Token 过期后如何刷新？

A: 使用 `refresh_token` 调用 `/public/auth/refresh` 接口获取新的 Token。如果 Refresh Token 也过期了，需要重新登录。

### Q4: Session ID 的作用是什么？

A: Session ID 用于标识用户会话，帮助后端管理密钥对。如果不提供 Session ID，后端会自动生成一个。

### Q5: 密码加密失败怎么办？

A: 检查公钥格式是否正确，确保使用正确的加密库（如 jsencrypt）。如果问题持续，请检查网络连接和服务器状态。

---

## 11. API 响应格式

所有 API 响应遵循统一格式：

```typescript
interface ApiResponse<T> {
  code: number;      // HTTP 状态码
  message: string;   // 响应消息
  data: T;          // 响应数据（成功时）或 null（错误时）
}
```

---

## 12. 联系支持

如有问题或建议，请联系开发团队或提交 Issue。

