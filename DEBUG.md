# 本地开发调试指南

本文档说明如何在本地开发环境中使用 IntelliJ IDEA 启动应用，同时使用 Docker Compose 启动数据库和 Redis 服务。

## 快速开始

### 1. 启动基础设施服务（数据库和 Redis）

```bash
# 启动数据库和 Redis 服务
docker compose -f docker-compose.debug.yml up -d

# 查看服务状态
docker compose -f docker-compose.debug.yml ps

# 停止服务
docker compose -f docker-compose.debug.yml down
```

### 2. 配置 IntelliJ IDEA

#### 方式一：使用环境变量配置文件（推荐）

1. 在项目根目录创建 `.env.debug` 文件（或使用现有的 `.env`）：

```bash
# 数据库配置
DB_NAME=spring-boot-kt
DB_USER=root
DB_PASSWORD=your_password
DB_PORT=3306

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0

# 应用配置
APP_PORT=8081
SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/spring-boot-kt
```

2. 在 IntelliJ IDEA 的运行配置中设置环境变量：
   - Run → Edit Configurations
   - 选择你的 Spring Boot 运行配置
   - 在 "Environment variables" 中添加：
     ```
     DB_NAME=spring-boot-kt
     DB_USER=root
     DB_PASSWORD=your_password
     SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/spring-boot-kt
     REDIS_HOST=localhost
     REDIS_PORT=6379
     REDIS_PASSWORD=
     REDIS_DATABASE=0
     APP_PORT=8081
     ```

#### 方式二：使用 VM options

在 IntelliJ IDEA 的运行配置中，VM options 添加：

```
-Dspring.profiles.active=dev
-DDB_NAME=spring-boot-kt
-DDB_USER=root
-DDB_PASSWORD=your_password
-DSPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/spring-boot-kt
-DREDIS_HOST=localhost
-DREDIS_PORT=6379
-DREDIS_PASSWORD=
-DREDIS_DATABASE=0
-DAPP_PORT=8081
```

### 3. 启动应用

在 IntelliJ IDEA 中直接运行 Spring Boot 应用的主类（通常是 `Application.kt`）。

## 服务连接信息

### 数据库（MariaDB）
- **主机**: `localhost`
- **端口**: `3306`
- **数据库名**: `spring-boot-kt`（或你在 `.env` 中配置的 `DB_NAME`）
- **用户名**: `root`（或你在 `.env` 中配置的 `DB_USER`）
- **密码**: 你在 `.env` 中配置的 `DB_PASSWORD`

### Redis
- **主机**: `localhost`
- **端口**: `6379`
- **密码**: 如果设置了 `REDIS_PASSWORD`，则使用该密码；否则为空
- **数据库**: `0`（或你在 `.env` 中配置的 `REDIS_DATABASE`）

## 验证服务

### 检查数据库连接

```bash
# 使用 MySQL 客户端连接
mysql -h localhost -P 3306 -u root -p

# 或使用 Docker 命令
docker exec -it mariadb-db mysql -u root -p
```

### 检查 Redis 连接

```bash
# 使用 redis-cli 连接
redis-cli -h localhost -p 6379

# 或使用 Docker 命令
docker exec -it redis-cache redis-cli
```

## 常见问题

### 1. 端口冲突

如果端口 3306 或 6379 已被占用，可以修改 `docker-compose.debug.yml` 中的端口映射：

```yaml
# 在 database/docker-compose.yml 中修改
ports:
  - "3307:3306"  # 使用 3307 端口

# 在 redis/docker-compose.yml 中修改
ports:
  - "6380:6379"  # 使用 6380 端口
```

然后相应地更新 IntelliJ IDEA 中的环境变量。

### 2. 数据库连接失败

确保：
- Docker 容器正在运行：`docker compose -f docker-compose.debug.yml ps`
- 数据库已初始化完成（等待健康检查通过）
- 环境变量配置正确

### 3. Redis 连接失败

确保：
- Redis 容器正在运行
- 如果设置了密码，环境变量 `REDIS_PASSWORD` 必须正确

## 优势

使用这种调试模式的好处：

1. ✅ **快速启动**：应用启动更快（不需要构建 Docker 镜像）
2. ✅ **热重载**：支持 Spring Boot DevTools 热重载
3. ✅ **断点调试**：可以在 IntelliJ IDEA 中设置断点调试
4. ✅ **代码修改即时生效**：修改代码后立即生效，无需重新构建镜像
5. ✅ **资源占用少**：只启动必要的服务（数据库和 Redis）

## 停止服务

```bash
# 停止并删除容器
docker compose -f docker-compose.debug.yml down

# 停止并删除容器和数据卷（注意：会删除数据）
docker compose -f docker-compose.debug.yml down -v
```

