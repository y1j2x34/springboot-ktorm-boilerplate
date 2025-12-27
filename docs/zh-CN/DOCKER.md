# Docker 部署说明

## 服务概览

该项目使用 docker compose 管理服务：

- **docker-compose.yml** - Spring Boot 应用和 MariaDB

### 服务列表

1. **springboot-app** - Spring Boot Kotlin 应用 (端口: 8081)
2. **mariadb** - MariaDB 数据库 (端口: 3306)

## 快速开始

### 方式 1: 使用便捷脚本（推荐）

#### 启动服务
```bash
./start-all.sh
```

#### 查看日志
```bash
# 查看所有服务日志
./logs.sh all

# 只查看 Spring Boot 日志
./logs.sh springboot
```

#### 停止服务
```bash
# 停止所有服务
./stop-all.sh

# 停止并删除数据卷
./stop-all.sh --volumes
```

### 方式 2: 手动启动

#### 启动服务
```bash
docker compose up -d
```

#### 查看日志
```bash
# 查看 Spring Boot 日志
docker compose logs -f springboot-app
```

#### 停止服务
```bash
# 停止 Spring Boot
docker compose down

# 停止并删除数据卷
docker compose down -v
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

项目使用 Docker 网络 `app-network`：
- 主 docker-compose.yml 创建网络

### 文件结构

```
.
├── docker-compose.yml              # Spring Boot 应用配置
├── Dockerfile                     # Spring Boot 应用构建文件
├── start-all.sh                   # 启动脚本
├── stop-all.sh                    # 停止脚本
├── logs.sh                        # 日志查看脚本
└── Makefile                       # Make 命令管理
```

## 服务访问

- Spring Boot API: http://localhost:8081/api
- MariaDB: localhost:3306

## 环境变量配置

可以通过创建 `.env` 文件来自定义配置：

```bash
# 在项目根目录创建 .env 文件
cat > .env << EOF
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

## 健康检查

所有服务都配置了健康检查：
- MariaDB: 每 10 秒检查一次
- Spring Boot App: 依赖 MariaDB 健康状态

## 故障排除

### 查看服务状态
```bash
# 使用 Makefile
make status

# 或手动查看
docker compose ps
```

### 重启服务
```bash
# 重启 Spring Boot
docker compose restart springboot-app

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

