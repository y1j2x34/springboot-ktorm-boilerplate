# 生产环境部署指南

本指南详细说明如何在生产环境中部署 Spring Boot Ktorm 应用。

## 📋 目录

- [系统要求](#系统要求)
- [部署前准备](#部署前准备)
- [快速部署](#快速部署)
- [详细配置](#详细配置)
- [监控和日志](#监控和日志)
- [安全加固](#安全加固)
- [备份和恢复](#备份和恢复)
- [故障排除](#故障排除)

## 系统要求

### 硬件要求

| 组件 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 2 核 | 4 核+ |
| 内存 | 4 GB | 8 GB+ |
| 磁盘 | 20 GB | 50 GB+ SSD |
| 网络 | 100 Mbps | 1 Gbps |

### 软件要求

- **操作系统**: Linux (Ubuntu 20.04+, CentOS 8+, Debian 11+)
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **系统工具**: curl, wget, git

## 部署前准备

### 1. 安装 Docker 和 Docker Compose

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 2. 克隆项目

```bash
git clone <your-repository>
cd springboot-ktorm-boilerplate
```

### 3. 配置环境变量

```bash
# 创建生产环境配置文件
cat > .env.prod << 'EOF'
# Application
VERSION=1.0.0
REGISTRY=your-registry.com/springboot-ktorm
APP_PORT=8081

# Database - 使用强密码！
DB_NAME=web_ai
DB_USER=appuser
DB_PASSWORD=<STRONG_PASSWORD_HERE>
DB_ROOT_PASSWORD=<STRONG_ROOT_PASSWORD_HERE>
DB_PORT=3306

# Data Directory
DATA_DIR=/var/lib/springboot-ktorm-app

# Logto
TAG=latest
ENDPOINT=https://auth.yourdomain.com
ADMIN_ENDPOINT=https://auth-admin.yourdomain.com
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<STRONG_POSTGRES_PASSWORD>
POSTGRES_DB=logto
EOF

# 保护配置文件
chmod 600 .env.prod
```

**重要安全提示:**
- ✅ 使用至少 16 字符的强密码
- ✅ 不同服务使用不同密码
- ✅ 永远不要将 `.env.prod` 提交到版本控制
- ✅ 定期轮换密码

### 4. 创建数据目录

```bash
sudo mkdir -p /var/lib/springboot-ktorm-app/{mariadb,postgres}
sudo chown -R $USER:$USER /var/lib/springboot-ktorm-app
chmod 700 /var/lib/springboot-ktorm-app/{mariadb,postgres}
```

## 快速部署

### 方式 1: 使用部署脚本（推荐）

```bash
# 基础部署
./deploy-prod.sh

# 包含监控栈
./deploy-prod.sh --with-monitoring

# 自定义环境文件
./deploy-prod.sh --env-file .env.custom
```

### 方式 2: 手动部署

```bash
# 1. 构建镜像
docker compose -f docker-compose.prod.yml build

# 2. 启动服务
docker compose -f docker-compose.prod.yml up -d

# 3. 启动 Logto
cd logto && docker compose -f docker-compose.prod.yml up -d && cd ..

# 4. (可选) 启动监控
docker compose -f docker-compose.prod.yml -f docker-compose.monitoring.yml up -d
```

## 详细配置

### 资源限制

每个服务都配置了资源限制，在 `docker-compose.prod.yml` 中调整:

```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'        # 最大 CPU 核数
      memory: 2560M      # 最大内存
    reservations:
      cpus: '0.5'        # 预留 CPU
      memory: 512M       # 预留内存
```

### 数据库优化

#### MariaDB 配置

编辑 `docker/mariadb/conf.d/custom.cnf`:

```ini
# 根据可用内存调整
innodb_buffer_pool_size = 1G  # 推荐为可用内存的 70-80%
max_connections = 500          # 根据并发量调整
```

#### PostgreSQL 配置

编辑 `logto/postgres/postgresql.conf`:

```ini
shared_buffers = 256MB         # 推荐为内存的 25%
effective_cache_size = 1GB     # 推荐为内存的 50-75%
max_connections = 200
```

### JVM 调优

在 `docker-compose.prod.yml` 中调整 `JAVA_OPTS`:

```yaml
environment:
  - JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

**推荐配置:**
- `-Xms`: 初始堆大小，设置为最大堆的 50%
- `-Xmx`: 最大堆大小，不超过容器内存的 75%
- `UseG1GC`: 推荐使用 G1 垃圾回收器

## 监控和日志

### 启动监控栈

```bash
# 部署时包含监控
./deploy-prod.sh --with-monitoring

# 或单独启动
docker compose -f docker-compose.monitoring.yml up -d
```

### 监控组件

| 服务 | 端口 | 说明 |
|------|------|------|
| Grafana | 3000 | 可视化仪表板 |
| Prometheus | 9090 | 指标收集 |
| Loki | 3100 | 日志聚合 |
| cAdvisor | 8080 | 容器监控 |

### 访问 Grafana

```bash
# 默认登录
URL: http://your-server:3000
用户名: admin
密码: admin (首次登录后修改)
```

### 查看日志

```bash
# 应用日志
docker compose -f docker-compose.prod.yml logs -f springboot-app

# 数据库日志
docker compose -f docker-compose.prod.yml logs -f mariadb

# 所有服务日志
docker compose -f docker-compose.prod.yml logs -f

# 导出日志
docker compose -f docker-compose.prod.yml logs --no-color > app.log
```

### 配置告警

编辑 `monitoring/prometheus/alerts.yml` 添加自定义告警规则。

## 安全加固

### 1. 防火墙配置

```bash
# UFW (Ubuntu)
sudo ufw allow 22/tcp      # SSH
sudo ufw allow 80/tcp      # HTTP
sudo ufw allow 443/tcp     # HTTPS
sudo ufw allow 8081/tcp    # Spring Boot (如需外部访问)
sudo ufw enable

# 限制访问
sudo ufw allow from 192.168.1.0/24 to any port 3306  # 数据库仅内网访问
```

### 2. SSL/TLS 配置

使用 Nginx 或 Traefik 作为反向代理，配置 SSL 证书:

```nginx
server {
    listen 443 ssl http2;
    server_name api.yourdomain.com;

    ssl_certificate /etc/ssl/certs/cert.pem;
    ssl_certificate_key /etc/ssl/private/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;

    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 3. 限制网络访问

在 `docker-compose.prod.yml` 中使用内部网络:

```yaml
networks:
  internal-network:
    driver: bridge
    internal: true  # 隔离内部网络
```

### 4. 定期更新

```bash
# 更新 Docker 镜像
docker compose -f docker-compose.prod.yml pull

# 重新部署
./deploy-prod.sh
```

## 备份和恢复

### 数据库备份

#### MariaDB 备份

```bash
# 创建备份目录
mkdir -p /backup/mariadb

# 备份脚本
cat > /backup/backup-mariadb.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/mariadb"
DB_NAME="web_ai"
DB_USER="root"
DB_PASSWORD="YOUR_ROOT_PASSWORD"

docker exec mariadb-db mysqldump \
  -u${DB_USER} -p${DB_PASSWORD} \
  --single-transaction \
  --quick \
  --lock-tables=false \
  ${DB_NAME} | gzip > ${BACKUP_DIR}/backup_${DATE}.sql.gz

# 保留最近 7 天的备份
find ${BACKUP_DIR} -name "backup_*.sql.gz" -mtime +7 -delete
EOF

chmod +x /backup/backup-mariadb.sh

# 设置 cron 任务 (每天 2:00 AM)
echo "0 2 * * * /backup/backup-mariadb.sh" | crontab -
```

#### PostgreSQL 备份

```bash
# 创建备份目录
mkdir -p /backup/postgres

# 备份脚本
cat > /backup/backup-postgres.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/postgres"
DB_NAME="logto"
DB_USER="postgres"

docker exec postgres-db pg_dump \
  -U ${DB_USER} \
  -d ${DB_NAME} \
  --format=custom | gzip > ${BACKUP_DIR}/backup_${DATE}.dump.gz

# 保留最近 7 天的备份
find ${BACKUP_DIR} -name "backup_*.dump.gz" -mtime +7 -delete
EOF

chmod +x /backup/backup-postgres.sh

# 设置 cron 任务
echo "0 2 * * * /backup/backup-postgres.sh" | crontab -
```

### 恢复数据

#### MariaDB 恢复

```bash
# 恢复备份
gunzip < /backup/mariadb/backup_20241224_020000.sql.gz | \
  docker exec -i mariadb-db mysql -uroot -pYOUR_PASSWORD web_ai
```

#### PostgreSQL 恢复

```bash
# 恢复备份
gunzip < /backup/postgres/backup_20241224_020000.dump.gz | \
  docker exec -i postgres-db pg_restore -U postgres -d logto --clean
```

### 数据卷备份

```bash
# 停止服务
./stop-prod.sh

# 备份数据目录
tar -czf /backup/volumes_$(date +%Y%m%d).tar.gz /var/lib/springboot-ktorm-app

# 重启服务
./deploy-prod.sh
```

## 故障排除

### 服务无法启动

```bash
# 检查日志
docker compose -f docker-compose.prod.yml logs

# 检查容器状态
docker compose -f docker-compose.prod.yml ps

# 检查资源使用
docker stats

# 检查磁盘空间
df -h
```

### 性能问题

```bash
# 检查数据库连接
docker exec mariadb-db mysqladmin -uroot -p processlist

# 检查慢查询日志
docker exec mariadb-db tail -f /var/log/mysql/slow-query.log

# 检查 JVM 堆使用
docker exec springboot-ktorm-app jstat -gc 1 1000
```

### 内存溢出

```bash
# 检查堆转储
docker exec springboot-ktorm-app ls -lh /app/logs/

# 下载堆转储分析
docker cp springboot-ktorm-app:/app/logs/heap-dump.hprof ./
```

### 网络问题

```bash
# 检查网络
docker network ls
docker network inspect springboot-ktorm-boilerplate_app-network

# 测试服务连通性
docker exec springboot-ktorm-app ping mariadb
docker exec springboot-ktorm-app curl http://localhost:8081/api/actuator/health
```

## 升级和回滚

### 升级应用

```bash
# 1. 备份数据
./stop-prod.sh
tar -czf backup_before_upgrade.tar.gz /var/lib/springboot-ktorm-app

# 2. 拉取最新代码
git pull origin main

# 3. 更新版本号
export VERSION=1.1.0

# 4. 重新部署
./deploy-prod.sh
```

### 回滚

```bash
# 1. 停止服务
./stop-prod.sh

# 2. 切换到旧版本
git checkout v1.0.0

# 3. 恢复数据（如需要）
tar -xzf backup_before_upgrade.tar.gz -C /

# 4. 重新部署
./deploy-prod.sh
```

## 性能调优建议

### 1. 数据库调优

- 定期执行 `ANALYZE TABLE` 更新统计信息
- 监控慢查询日志，优化 SQL
- 适当增加连接池大小
- 启用查询缓存（适用场景）

### 2. 应用调优

- 启用 HTTP/2
- 配置响应压缩
- 使用 CDN 加速静态资源
- 实施缓存策略（Redis）

### 3. 系统调优

```bash
# 增加文件描述符限制
echo "* soft nofile 65536" >> /etc/security/limits.conf
echo "* hard nofile 65536" >> /etc/security/limits.conf

# 优化 TCP 参数
cat >> /etc/sysctl.conf << EOF
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_fin_timeout = 30
net.core.somaxconn = 1024
EOF

sysctl -p
```

## 维护计划

### 日常维护

- ✅ 检查服务状态
- ✅ 查看错误日志
- ✅ 监控资源使用
- ✅ 检查磁盘空间

### 周维护

- ✅ 检查备份完整性
- ✅ 审查慢查询日志
- ✅ 更新安全补丁
- ✅ 检查告警配置

### 月维护

- ✅ 性能测试和优化
- ✅ 数据库优化
- ✅ 安全审计
- ✅ 文档更新

## 支持和帮助

遇到问题？

1. 查看日志: `docker compose -f docker-compose.prod.yml logs`
2. 检查健康状态: `curl http://localhost:8081/api/actuator/health`
3. 查看监控面板: Grafana Dashboard
4. 提交 Issue: [GitHub Issues](your-repo-issues)

---

**重要提醒:**
- 生产环境部署前请充分测试
- 定期备份数据
- 监控系统运行状态
- 及时更新安全补丁
- 保护敏感信息安全

