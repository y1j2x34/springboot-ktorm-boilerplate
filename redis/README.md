# Redis Docker 配置

Redis 服务的 Docker Compose 配置文件。

## 文件说明

- `docker-compose.yml`: 开发环境配置
- `docker-compose.prod.yml`: 生产环境配置
- `entrypoint.sh`: Redis 启动脚本（处理密码配置）

## 开发环境使用

### 启动 Redis

```bash
# 从项目根目录启动
docker compose -f docker-compose.yml up -d redis

# 或者启动所有服务（包括 Redis）
docker compose up -d
```

### 环境变量

在 `.env` 文件中配置（可选）：

```bash
REDIS_PORT=6379
REDIS_PASSWORD=  # 留空表示不使用密码
REDIS_DATABASE=0
```

### 连接 Redis

```bash
# 无密码连接
docker exec -it redis-cache redis-cli

# 有密码连接
docker exec -it redis-cache redis-cli -a YOUR_PASSWORD
```

## 生产环境使用

### 配置环境变量

1. 复制环境变量示例文件：
```bash
cp env.prod.example .env.prod
```

2. 编辑 `.env.prod`，设置 Redis 密码：
```bash
REDIS_PASSWORD=your_strong_password_here
REDIS_PORT=6379
REDIS_DATABASE=0
```

3. 设置文件权限：
```bash
chmod 600 .env.prod
```

### 启动服务

```bash
# 使用生产配置启动
docker compose -f docker-compose.prod.yml up -d redis
```

## 配置说明

### 开发环境特性

- 数据持久化：启用 AOF（Append Only File）
- 密码：可选（通过环境变量配置）
- 网络：使用 `app-network`（可外部访问）

### 生产环境特性

- 数据持久化：AOF + RDB 快照
- 密码：必需（强制要求）
- 内存限制：512MB，使用 LRU 淘汰策略
- 网络：使用 `internal-network`（仅内部访问）
- 资源限制：CPU 1.0，内存 1GB
- 日志轮转：50MB，保留 3 个文件
- 安全：`no-new-privileges` 安全选项

## 数据持久化

Redis 数据存储在 Docker volume 中：

- 开发环境：`redis-data` volume（Docker 管理）
- 生产环境：`${DATA_DIR}/redis` 目录（绑定挂载）

## 健康检查

Redis 容器包含健康检查，确保服务正常运行：

- 检查命令：`redis-cli ping`
- 检查间隔：10 秒
- 超时时间：3 秒
- 重试次数：5 次

## 故障排查

### 查看日志

```bash
docker logs redis-cache
```

### 检查连接

```bash
# 测试连接
docker exec -it redis-cache redis-cli ping

# 查看 Redis 信息
docker exec -it redis-cache redis-cli INFO
```

### 重置数据

```bash
# 停止并删除容器和数据卷
docker compose down -v

# 重新启动
docker compose up -d redis
```

## 注意事项

1. **生产环境必须设置强密码**
2. **定期备份 Redis 数据**
3. **监控内存使用情况**
4. **生产环境使用内部网络，不对外暴露端口**

