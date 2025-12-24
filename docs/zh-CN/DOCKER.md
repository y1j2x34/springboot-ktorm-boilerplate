# Docker 部署说明

## 服务概览

该项目使用多个 docker compose 文件管理不同的服务：

- **docker-compose.yml** - Spring Boot 应用和 MariaDB
- **logto/docker-compose.yml** - Logto 认证服务和 PostgreSQL

### 服务列表

1. **springboot-app** - Spring Boot Kotlin 应用 (端口: 8081)
2. **mariadb** - MariaDB 数据库 (端口: 3306)
3. **logto-app** - Logto 认证服务 (端口: 3001, 3002)
4. **postgres** - PostgreSQL 数据库 for Logto (端口: 5432)

## 快速开始

### 方式 1: 使用便捷脚本（推荐）

#### 启动所有服务
```bash
./start-all.sh
```

#### 查看日志
```bash
# 查看所有服务日志
./logs.sh all

# 只查看 Spring Boot 日志
./logs.sh springboot

# 只查看 Logto 日志
./logs.sh logto
```

#### 停止服务
```bash
# 停止所有服务
./stop-all.sh

# 停止并删除数据卷
./stop-all.sh --volumes
```

### 方式 2: 手动启动

#### 启动所有服务
```bash
# 1. 启动 Spring Boot 应用（会创建共享网络）
docker compose up -d

# 2. 启动 Logto 服务
cd logto && docker compose up -d && cd ..
```

#### 启动特定服务
```bash
# 只启动 Spring Boot 应用和 MariaDB
docker compose up -d

# 只启动 Logto 和 PostgreSQL
cd logto && docker compose up -d && cd ..
```

#### 查看日志
```bash
# 查看 Spring Boot 日志
docker compose logs -f springboot-app

# 查看 Logto 日志
cd logto && docker compose logs -f app
```

#### 停止服务
```bash
# 停止 Logto
cd logto && docker compose down && cd ..

# 停止 Spring Boot
docker compose down

# 停止并删除数据卷
docker compose down -v
cd logto && docker compose down -v && cd ..
```

### 方式 3: 使用 Makefile

```bash
# 查看所有可用命令
make help

# 启动所有服务
make start

# 查看日志
make logs

# 停止服务
make stop
```

## 架构说明

### 网络配置

两个 docker compose 文件共享同一个 Docker 网络 `app-network`：
- 主 docker-compose.yml 创建网络
- logto/docker-compose.yml 连接到外部网络

这样所有服务可以互相通信，例如 Spring Boot 应用可以访问 Logto 的认证服务。

### 文件结构

```
.
├── docker-compose.yml              # Spring Boot 应用配置
├── logto/
│   └── docker-compose.yml         # Logto 服务配置（独立管理）
├── Dockerfile                     # Spring Boot 应用构建文件
├── start-all.sh                   # 启动脚本
├── stop-all.sh                    # 停止脚本
├── logs.sh                        # 日志查看脚本
└── Makefile                       # Make 命令管理
```

## 服务访问

- Spring Boot API: http://localhost:8081/api
- Logto 管理界面: http://localhost:3002
- Logto API: http://localhost:3001
- MariaDB: localhost:3306
- PostgreSQL: localhost:5432

## 环境变量配置

可以通过创建 `.env` 文件来自定义配置：

```bash
# 在项目根目录创建 .env 文件
cat > .env << EOF
# Logto 配置
TAG=latest
ENDPOINT=http://localhost:3001
ADMIN_ENDPOINT=http://localhost:3002

# MariaDB 配置
MYSQL_ROOT_PASSWORD=zhang1128!
MYSQL_DATABASE=web_ai

# Spring Boot 配置
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8081
EOF
```

Docker Compose 会自动读取 `.env` 文件中的环境变量。

## 数据持久化

数据卷：
- `mariadb-data`: MariaDB 数据存储
- `postgres-data`: PostgreSQL 数据存储

## 健康检查

所有服务都配置了健康检查：
- MariaDB: 每 10 秒检查一次
- PostgreSQL: 每 5 秒检查一次
- Spring Boot App: 依赖 MariaDB 健康状态
- Logto App: 依赖 PostgreSQL 健康状态

## 故障排除

### 查看服务状态
```bash
# 使用 Makefile
make status

# 或手动查看
docker compose ps
cd logto && docker compose ps
```

### 网络问题

如果 Logto 无法连接到共享网络，请确保先启动主 docker-compose：

```bash
# 先启动主服务（创建网络）
docker compose up -d

# 再启动 Logto
cd logto && docker compose up -d
```

### 重启服务
```bash
# 重启 Spring Boot
docker compose restart springboot-app

# 重启 Logto
cd logto && docker compose restart app

# 重启所有服务
make restart
```

### 重新构建应用
```bash
# 使用 Makefile
make build

# 或手动构建
docker compose build --no-cache springboot-app
docker compose up -d springboot-app
```

### 进入容器调试
```bash
# 进入 Spring Boot 容器
docker compose exec springboot-app sh

# 进入 MariaDB
docker compose exec mariadb mysql -uroot -pzhang1128!

# 进入 Logto 容器
cd logto && docker compose exec app sh

# 进入 PostgreSQL
cd logto && docker compose exec postgres psql -U postgres -d logto
```

### 清理和重置

```bash
# 停止并删除所有数据
make stop-clean

# 或手动清理
./stop-all.sh --volumes

# 完全清理（包括未使用的镜像）
make clean
```

## 常见问题

### Q: 为什么使用两个 docker compose 文件？

A: 为了更好的模块化管理。`logto/docker-compose.yml` 是独立的认证服务配置，可以单独维护和更新，而不影响主应用。两者通过共享 Docker 网络进行通信。

### Q: 如何只启动 Spring Boot 应用？

A: 只运行主 docker compose 即可：
```bash
docker compose up -d
```

### Q: 如何只启动 Logto 服务？

A: 先确保网络已创建，然后启动 Logto：
```bash
docker network create springboot-ktorm-boilerplate_app-network
cd logto && docker compose up -d
```

### Q: 服务之间如何通信？

A: 所有服务都在 `app-network` 网络中，可以使用服务名称互相访问：
- Spring Boot 可以通过 `http://logto-app:3001` 访问 Logto
- Logto 可以通过 `http://springboot-app:8081` 访问 Spring Boot

### Q: 数据会丢失吗？

A: 不会。数据存储在 Docker 卷中（`mariadb-data` 和 `postgres-data`），除非使用 `--volumes` 参数明确删除。

