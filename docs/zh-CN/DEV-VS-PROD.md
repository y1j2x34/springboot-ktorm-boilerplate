# 开发环境 vs 生产环境配置对比

## 📋 快速对比

| 特性 | 开发环境 | 生产环境 |
|------|---------|---------|
| **配置文件** | `docker-compose.yml` | `docker-compose.prod.yml` |
| **Dockerfile** | `Dockerfile` | `Dockerfile.prod` |
| **环境变量** | `.env` (可选) | `.env.prod` (必需) |
| **启动脚本** | `start-all.sh` | `deploy-prod.sh` |
| **资源限制** | ❌ 无 | ✅ 有（CPU、内存） |
| **安全加固** | ❌ 无 | ✅ 有（非 root、网络隔离） |
| **健康检查** | ⚠️ 基础 | ✅ 完整 |
| **监控** | ❌ 无 | ✅ Prometheus + Grafana + Loki |
| **日志管理** | ⚠️ 基础 | ✅ 轮转、限制、聚合 |
| **备份策略** | ❌ 无 | ✅ 自动备份 |

## 🔧 配置文件对比

### docker compose 配置

#### 开发环境 (`docker-compose.yml`)

```yaml
services:
  springboot-app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8081:8081"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mariadb://mariadb:3306/web_ai
      - SPRING_DATASOURCE_PASSWORD=zhang1128!  # 硬编码
    networks:
      - app-network
    restart: unless-stopped
    # ❌ 无资源限制
    # ❌ 无安全配置
    # ❌ 无日志管理
```

#### 生产环境 (`docker-compose.prod.yml`)

```yaml
services:
  springboot-app:
    build:
      dockerfile: Dockerfile.prod
    ports:
      - "${APP_PORT:-8081}:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD:?Required}  # 环境变量
      - JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC
    networks:
      - app-network
      - internal-network
    restart: unless-stopped
    # ✅ 资源限制
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2560M
    # ✅ 健康检查
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:8081/api/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    # ✅ 日志管理
    logging:
      driver: "json-file"
      options:
        max-size: "100m"
        max-file: "5"
    # ✅ 安全配置
    security_opt:
      - no-new-privileges:true
    tmpfs:
      - /tmp:noexec,nosuid,size=512m
```

### Dockerfile 对比

#### 开发环境 (`Dockerfile`)

```dockerfile
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle :app:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/app/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 生产环境 (`Dockerfile.prod`)

```dockerfile
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
# ✅ 依赖缓存优化
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
RUN gradle dependencies --no-daemon || true
COPY . .
RUN gradle :app:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
# ✅ 安装必要工具
RUN apk add --no-cache curl tzdata
# ✅ 时区配置
ENV TZ=Asia/Shanghai
# ✅ 非 root 用户
RUN addgroup -g 1000 -S spring && adduser -u 1000 -S spring -G spring
USER spring:spring

WORKDIR /app
COPY --from=builder --chown=spring:spring /app/build/app.jar /app/app.jar

# ✅ 元数据标签
LABEL version="${VERSION}" build-date="${BUILD_DATE}"

# ✅ 详细健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/api/actuator/health || exit 1

# ✅ JVM 优化参数
ENV JAVA_OPTS="-server -Xms512m -Xmx2g -XX:+UseG1GC ..."

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
```

## 🔐 安全性对比

| 安全特性 | 开发环境 | 生产环境 |
|---------|---------|---------|
| **密码管理** | 硬编码在配置文件 | 环境变量，必须设置 |
| **用户权限** | 默认用户（通常是 root） | 非 root 用户 (UID 1000) |
| **网络隔离** | 单一网络 | 内外网隔离 |
| **安全选项** | 无 | no-new-privileges, read-only |
| **端口暴露** | 所有端口暴露 | 最小化端口暴露 |
| **SSL/TLS** | 无 | 推荐配置 |

## 📊 监控和日志对比

### 开发环境

- **日志**: 输出到 stdout，无限制
- **监控**: 无专门监控
- **告警**: 无
- **日志聚合**: 无

### 生产环境

- **日志**: 
  - 大小限制（100MB per file）
  - 文件数量限制（5 个文件）
  - 集中收集（Loki + Promtail）
  - 结构化日志

- **监控**:
  - Prometheus（指标收集）
  - Grafana（可视化）
  - cAdvisor（容器监控）
  - Node Exporter（主机监控）

- **告警**:
  - 应用告警（内存、CPU、错误率）
  - 数据库告警（连接池、慢查询）
  - 主机告警（磁盘、负载）

## 💾 数据管理对比

### 开发环境

```yaml
volumes:
  mariadb-data:  # Docker 管理的卷
```

- 数据存储在 Docker 默认位置
- 无备份策略
- 数据可能丢失

### 生产环境

```yaml
volumes:
  mariadb-data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: ${DATA_DIR}/mariadb  # 绑定到指定目录
```

- 数据存储在指定位置（`/var/lib/springboot-ktorm-app`）
- 自动备份脚本
- 备份保留策略（7天）
- 易于迁移和恢复

## 🚀 部署流程对比

### 开发环境部署

```bash
# 简单启动
./start-all.sh

# 或者
docker compose up -d
```

**特点**:
- ✅ 快速启动
- ✅ 即时反馈
- ❌ 无检查验证
- ❌ 无健康确认

### 生产环境部署

```bash
# 完整部署流程
./deploy-prod.sh --with-monitoring
```

**流程包括**:
1. ✅ 环境检查（Docker、环境变量）
2. ✅ 前置验证（密码、配置）
3. ✅ 数据目录创建
4. ✅ 镜像构建（带缓存优化）
5. ✅ 优雅停止旧版本
6. ✅ 启动新版本
7. ✅ 健康检查等待
8. ✅ 状态验证
9. ✅ 日志输出

## 🔧 配置文件对比

### Spring Boot 配置

#### 开发环境 (`application.yml`)

```yaml
logging:
  level:
    root: DEBUG           # 详细日志
    org.ktorm: DEBUG

spring:
  datasource:
    url: jdbc:mariadb://127.0.0.1:3306/web_ai
    password: "zhang1128!"  # 硬编码

server:
  port: 8081
```

#### 生产环境 (`application-prod.yml`)

```yaml
logging:
  level:
    root: INFO            # 适当日志级别
    org.ktorm: INFO
  file:
    name: /app/logs/application.log
    max-size: 100MB       # 日志轮转
    max-history: 30

spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}  # 环境变量
    password: ${SPRING_DATASOURCE_PASSWORD}
    hikari:                # 连接池优化
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

server:
  port: ${SERVER_PORT}
  compression:
    enabled: true         # 响应压缩
  tomcat:
    threads:
      max: 200            # 线程池优化

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus  # 监控端点
```

## 📈 性能优化对比

### JVM 参数

#### 开发环境
```bash
# 默认 JVM 参数
java -jar app.jar
```

#### 生产环境
```bash
# 优化的 JVM 参数
java -server \
  -Xms512m \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/logs/heap-dump.hprof \
  -jar app.jar
```

### 数据库配置

#### 开发环境（MariaDB）
```ini
# 默认配置
max_connections = 151
innodb_buffer_pool_size = 128M
```

#### 生产环境（MariaDB）
```ini
# 优化配置
max_connections = 500
innodb_buffer_pool_size = 1G
innodb_buffer_pool_instances = 4
slow_query_log = 1
long_query_time = 2
```

## 🛠️ 运维工具对比

### 开发环境

**可用脚本**:
- `start-all.sh` - 启动所有服务
- `stop-all.sh` - 停止所有服务
- `logs.sh` - 查看日志
- `Makefile` - Make 命令

**特点**: 简单易用，快速迭代

### 生产环境

**可用脚本**:
- `deploy-prod.sh` - 完整部署流程
- `stop-prod.sh` - 优雅停止
- 备份脚本（MariaDB）
- 监控配置

**文档**:
- `PRODUCTION.md` - 完整指南
- `PRODUCTION-CHECKLIST.md` - 检查清单
- `README-PRODUCTION.md` - 快速参考

**特点**: 完善的运维支持

## 🎯 使用场景

### 使用开发环境配置

- ✅ 本地开发
- ✅ 功能测试
- ✅ 快速原型
- ✅ 学习和实验

### 使用生产环境配置

- ✅ 生产部署
- ✅ 预发布环境
- ✅ 性能测试
- ✅ 压力测试
- ✅ 安全测试

## 🔄 从开发到生产的迁移

### 步骤 1: 准备环境配置

```bash
# 复制生产环境变量模板
cp env.prod.example .env.prod

# 编辑配置
vim .env.prod
```

### 步骤 2: 测试生产配置

```bash
# 在测试环境验证
./deploy-prod.sh
```

### 步骤 3: 配置监控

```bash
# 启动监控栈
./deploy-prod.sh --with-monitoring
```

### 步骤 4: 配置备份

```bash
# 设置自动备份
crontab -e
# 添加: 0 2 * * * /backup/backup-mariadb.sh
```

### 步骤 5: 配置告警

编辑 `monitoring/prometheus/alerts.yml` 配置告警规则

### 步骤 6: 正式部署

完成 `PRODUCTION-CHECKLIST.md` 中的所有检查项

## 💡 最佳实践

1. **开发环境**: 保持简单，快速迭代
2. **生产环境**: 完整配置，安全第一
3. **测试环境**: 使用生产配置，数据隔离
4. **CI/CD**: 自动化部署流程
5. **文档**: 及时更新配置变更

## 📞 需要帮助？

- **开发环境**: 查看 `DOCKER.md` 和 `DOCKER-QUICKSTART.md`
- **生产环境**: 查看 `PRODUCTION.md` 和 `PRODUCTION-CHECKLIST.md`
- **对比总结**: 查看 `PRODUCTION-SUMMARY.md`

---

**记住**: 永远不要在生产环境使用开发配置！

