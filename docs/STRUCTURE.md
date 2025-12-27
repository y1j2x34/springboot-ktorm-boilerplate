# Project Structure / 项目结构

Organized documentation and configuration files for Spring Boot Ktorm Boilerplate.

为 Spring Boot Ktorm 脚手架项目整理的文档和配置文件。

## 📁 Directory Structure / 目录结构

```
springboot-ktorm-boilerplate/
├── README.md                          # Main project readme / 项目主说明
│
├── docs/                              # All documentation / 所有文档
│   ├── INDEX.md                       # Main documentation index / 主文档索引
│   ├── STRUCTURE.md                   # This file / 本文件
│   ├── en/                            # English documentation / 英文文档
│   │   ├── INDEX.md                   # English index / 英文索引
│   │   ├── README.md                  # Production quick start / 生产快速开始
│   │   ├── DOCKER-QUICKSTART.md       # Development quick start / 开发快速开始
│   │   ├── PRODUCTION.md              # Complete production guide / 完整生产指南
│   │   ├── PRODUCTION-CHECKLIST.md    # Deployment checklist / 部署检查清单
│   │   ├── PRODUCTION-SUMMARY.md      # Configuration summary / 配置总结
│   │   ├── DEV-VS-PROD.md             # Environment comparison / 环境对比
│   │   └── CHANGELOG-PRODUCTION.md    # Changelog / 更新日志
│   └── zh-CN/                         # Chinese documentation / 中文文档
│       ├── INDEX.md                   # Chinese index / 中文索引
│       ├── README-PRODUCTION.md       # 生产快速开始
│       ├── DOCKER-QUICKSTART.md       # 开发快速开始
│       ├── DOCKER.md                  # Docker 完整指南
│       ├── PRODUCTION.md              # 完整生产指南
│       ├── PRODUCTION-CHECKLIST.md    # 部署检查清单
│       ├── PRODUCTION-SUMMARY.md      # 配置总结
│       ├── DEV-VS-PROD.md             # 环境对比
│       └── CHANGELOG-PRODUCTION.md    # 更新日志
│
├── docker/                            # Docker related files / Docker 相关文件
│   └── configs/                       # Database configurations / Database 配置
│       └── mariadb/                   # MariaDB configurations / MariaDB 配置
│           └── conf.d/
│               └── custom.cnf         # MariaDB production config / 生产配置
│
├── monitoring/                        # Monitoring configurations / 监控配置
│   ├── prometheus/                    # Prometheus configuration / Prometheus 配置
│   │   ├── prometheus.yml             # Main config / 主配置
│   │   └── alerts.yml                 # Alert rules / 告警规则
│   ├── grafana/                       # Grafana configuration / Grafana 配置
│   │   └── provisioning/
│   │       ├── datasources/           # Data sources / 数据源
│   │       └── dashboards/            # Dashboards / 仪表板
│   ├── loki/                          # Loki configuration / Loki 配置
│   │   └── loki-config.yml
│   └── promtail/                      # Promtail configuration / Promtail 配置
│       └── promtail-config.yml
│
├── app/                               # Main application / 主应用
│   └── src/main/resources/
│       ├── application.yml            # Base config / 基础配置
│       ├── application-docker.yml     # Docker development config / Docker 开发配置
│       └── application-prod.yml       # Production config / 生产配置
│
├── common/                            # Common modules / 公共模块
├── captcha/                           # Captcha module / 验证码模块
├── jwt-auth/                          # JWT authentication / JWT 认证
├── user/                              # User module / 用户模块
│
├── docker-compose.yml                 # Development environment / 开发环境
├── docker-compose.prod.yml            # Production environment / 生产环境
├── docker-compose.monitoring.yml      # Monitoring stack / 监控栈
│
├── Dockerfile                         # Development image / 开发镜像
├── Dockerfile.prod                    # Production image / 生产镜像
├── .dockerignore                      # Docker ignore file / Docker 忽略文件
│
├── env.prod.example                   # Production env template / 生产环境变量模板
│
├── start-all.sh                       # Development start script / 开发启动脚本
├── stop-all.sh                        # Development stop script / 开发停止脚本
├── logs.sh                            # Log viewing script / 日志查看脚本
├── deploy-prod.sh                     # Production deployment / 生产部署脚本
├── stop-prod.sh                       # Production stop script / 生产停止脚本
│
├── Makefile                           # Make commands / Make 命令
├── build.gradle.kts                   # Gradle build file / Gradle 构建文件
└── settings.gradle.kts                # Gradle settings / Gradle 设置
```

## 📂 Key Directories / 关键目录

### Documentation / 文档目录 (`docs/`)

**Purpose / 用途**: All project documentation, organized by language.

**Structure / 结构**:
- `INDEX.md`: Main index for all documentation / 所有文档的主索引
- `STRUCTURE.md`: This file, explains project structure / 本文件，说明项目结构
- `en/`: English documentation / 英文文档
- `zh-CN/`: Chinese documentation / 中文文档

### Docker Configurations / Docker 配置 (`docker/`)

**Purpose / 用途**: Database and service configurations for Docker containers.

**Structure / 结构**:
- `configs/mariadb/`: MariaDB production optimizations

### Monitoring / 监控 (`monitoring/`)

**Purpose / 用途**: Complete monitoring stack configurations.

**Includes / 包含**:
- Prometheus (metrics collection / 指标收集)
- Grafana (visualization / 可视化)
- Loki (log aggregation / 日志聚合)
- Promtail (log collection / 日志收集)

## 📄 Key Files / 关键文件

### Development / 开发环境

| File | Purpose |
|------|---------|
| `docker-compose.yml` | Development services configuration |
| `Dockerfile` | Development image build |
| `start-all.sh` | Start all development services |
| `stop-all.sh` | Stop all development services |
| `logs.sh` | View service logs |

### Production / 生产环境

| File | Purpose |
|------|---------|
| `docker-compose.prod.yml` | Production services with resource limits |
| `docker-compose.monitoring.yml` | Monitoring stack configuration |
| `Dockerfile.prod` | Production optimized image |
| `env.prod.example` | Production environment variable template |
| `deploy-prod.sh` | Automated production deployment |
| `stop-prod.sh` | Production services shutdown |

### Configuration / 配置文件

| File | Environment | Purpose |
|------|-------------|---------|
| `application.yml` | Base | Common configurations |
| `application-docker.yml` | Development | Docker-specific settings |
| `application-prod.yml` | Production | Production optimizations |
| `docker/configs/mariadb/conf.d/custom.cnf` | Production | MariaDB tuning |

## 🔍 Finding Files / 查找文件

### By Purpose / 按用途

**Starting Development / 开始开发**:
- Documentation: `docs/en/DOCKER-QUICKSTART.md` or `docs/zh-CN/DOCKER-QUICKSTART.md`
- Script: `./start-all.sh`
- Config: `docker-compose.yml`

**Production Deployment / 生产部署**:
- Documentation: `docs/en/PRODUCTION.md` or `docs/zh-CN/PRODUCTION.md`
- Script: `./deploy-prod.sh`
- Config: `docker-compose.prod.yml`, `env.prod.example`

**Monitoring / 监控**:
- Config: `docker-compose.monitoring.yml`
- Prometheus: `monitoring/prometheus/`
- Grafana: `monitoring/grafana/`

**Database Configuration / 数据库配置**:
- MariaDB: `docker/configs/mariadb/conf.d/custom.cnf`

### By Language / 按语言

**English Readers**:
- Start here: `docs/en/INDEX.md`
- Quick start: `docs/en/DOCKER-QUICKSTART.md`
- Production: `docs/en/PRODUCTION.md`

**中文读者**:
- 从这里开始: `docs/zh-CN/INDEX.md`
- 快速开始: `docs/zh-CN/DOCKER-QUICKSTART.md`
- 生产部署: `docs/zh-CN/PRODUCTION.md`

## 🎯 Design Principles / 设计原则

1. **Separation of Concerns / 关注点分离**
   - Documentation separate from code / 文档与代码分离
   - Development and production configurations separated / 开发和生产配置分离
   - Language-specific documentation organized / 语言特定文档组织

2. **Clear Organization / 清晰组织**
   - Logical directory structure / 逻辑目录结构
   - Consistent naming conventions / 一致的命名约定
   - Easy to navigate / 易于导航

3. **Bilingual Support / 双语支持**
   - Complete English documentation / 完整英文文档
   - Complete Chinese documentation / 完整中文文档
   - Parallel structure / 平行结构

4. **Production Ready / 生产就绪**
   - Separate production configurations / 独立生产配置
   - Security best practices / 安全最佳实践
   - Complete monitoring setup / 完整监控设置

## 🔄 File Relationships / 文件关系

```
README.md (Project Overview)
    ├── docs/INDEX.md (Documentation Hub)
    │   ├── docs/en/INDEX.md (English Docs)
    │   └── docs/zh-CN/INDEX.md (Chinese Docs)
    │
    ├── docker-compose.yml (Dev Environment)
    │   ├── Dockerfile (Dev Image)
    │   └── app/src/main/resources/application-docker.yml
    │
    ├── docker-compose.prod.yml (Prod Environment)
    │   ├── Dockerfile.prod (Prod Image)
    │   ├── env.prod.example (Env Template)
    │   ├── docker/configs/ (DB Configs)
    │   └── app/src/main/resources/application-prod.yml
    │
    └── docker-compose.monitoring.yml (Monitoring)
        └── monitoring/ (Monitoring Configs)
```

## 📞 Related Documentation / 相关文档

- **[Main Documentation Index / 主文档索引](INDEX.md)**
- **[English Documentation / 英文文档](en/INDEX.md)**
- **[Chinese Documentation / 中文文档](zh-CN/INDEX.md)**

---

*Last updated / 最后更新: 2024-12-24*

