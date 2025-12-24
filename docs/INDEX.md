# 📚 Documentation Index / 文档索引

Complete documentation for Spring Boot Ktorm Boilerplate project, available in English and Chinese.

完整的 Spring Boot Ktorm 脚手架项目文档，提供英文和中文版本。

---

## 🌍 Choose Your Language / 选择语言

### English Documentation → [docs/en/](en/)
- Quick Start Guide
- Production Deployment
- Configuration References
- Troubleshooting

### 中文文档 → [docs/zh-CN/](zh-CN/)
- 快速开始指南
- 生产环境部署
- 配置参考
- 故障排除

---

## 📖 Quick Navigation / 快速导航

### For Developers / 开发者

| Task | English | 中文 |
|------|---------|------|
| **Start Development** | [Quick Start](en/DOCKER-QUICKSTART.md) | [快速开始](zh-CN/DOCKER-QUICKSTART.md) |
| **Docker Guide** | [Docker Guide](en/DOCKER.md) | [Docker 指南](zh-CN/DOCKER.md) |

### For DevOps / 运维人员

| Task | English | 中文 |
|------|---------|------|
| **Production Deployment** | [Production Guide](en/README.md) | [生产部署](zh-CN/README-PRODUCTION.md) |
| **Pre-deployment Check** | [Checklist](en/PRODUCTION-CHECKLIST.md) | [检查清单](zh-CN/PRODUCTION-CHECKLIST.md) |
| **Complete Guide** | [Full Guide](en/PRODUCTION.md) | [完整指南](zh-CN/PRODUCTION.md) |

### For Everyone / 所有人

| Topic | English | 中文 |
|-------|---------|------|
| **Dev vs Prod** | [Comparison](en/DEV-VS-PROD.md) | [环境对比](zh-CN/DEV-VS-PROD.md) |
| **Summary** | [Summary](en/PRODUCTION-SUMMARY.md) | [配置总结](zh-CN/PRODUCTION-SUMMARY.md) |
| **Changelog** | [Changelog](en/CHANGELOG-PRODUCTION.md) | [更新日志](zh-CN/CHANGELOG-PRODUCTION.md) |

---

## 🚀 Quick Commands / 快速命令

### Development Environment / 开发环境

```bash
# Start all services / 启动所有服务
./start-all.sh

# View logs / 查看日志
./logs.sh all

# Stop services / 停止服务
./stop-all.sh
```

### Production Environment / 生产环境

```bash
# Deploy / 部署
./deploy-prod.sh

# Deploy with monitoring / 部署含监控
./deploy-prod.sh --with-monitoring

# Stop / 停止
./stop-prod.sh
```

---

## 📁 Documentation Structure / 文档结构

```
docs/
├── INDEX.md                    # This file / 本文件
├── en/                         # English documentation
│   ├── README.md               # Production quick start
│   ├── DOCKER-QUICKSTART.md    # Development quick start
│   ├── PRODUCTION.md           # Complete production guide
│   ├── PRODUCTION-CHECKLIST.md # Deployment checklist
│   ├── PRODUCTION-SUMMARY.md   # Configuration summary
│   ├── DEV-VS-PROD.md          # Environment comparison
│   ├── CHANGELOG-PRODUCTION.md # Changelog
│   └── INDEX.md                # English index
└── zh-CN/                      # 中文文档
    ├── README-PRODUCTION.md    # 生产快速开始
    ├── DOCKER-QUICKSTART.md    # 开发快速开始
    ├── PRODUCTION.md           # 完整生产指南
    ├── PRODUCTION-CHECKLIST.md # 部署检查清单
    ├── PRODUCTION-SUMMARY.md   # 配置总结
    ├── DEV-VS-PROD.md          # 环境对比
    ├── CHANGELOG-PRODUCTION.md # 更新日志
    └── INDEX.md                # 中文索引
```

---

## 🎯 Usage Scenarios / 使用场景

### Scenario 1: New Developer / 新开发者

**Goal**: Quick start for local development  
**目标**: 快速开始本地开发

**Steps / 步骤**:
1. Read Quick Start → [EN](en/DOCKER-QUICKSTART.md) | [CN](zh-CN/DOCKER-QUICKSTART.md)
2. Run `./start-all.sh`
3. Start coding!

### Scenario 2: First Production Deployment / 首次生产部署

**Goal**: Deploy to production safely  
**目标**: 安全部署到生产环境

**Steps / 步骤**:
1. Read Production Guide → [EN](en/PRODUCTION.md) | [CN](zh-CN/PRODUCTION.md)
2. Complete Checklist → [EN](en/PRODUCTION-CHECKLIST.md) | [CN](zh-CN/PRODUCTION-CHECKLIST.md)
3. Run `./deploy-prod.sh --with-monitoring`
4. Verify deployment

### Scenario 3: Understanding Differences / 理解差异

**Goal**: Understand dev vs prod configurations  
**目标**: 理解开发和生产环境的配置差异

**Read / 阅读**:
- Comparison → [EN](en/DEV-VS-PROD.md) | [CN](zh-CN/DEV-VS-PROD.md)
- Summary → [EN](en/PRODUCTION-SUMMARY.md) | [CN](zh-CN/PRODUCTION-SUMMARY.md)

### Scenario 4: Troubleshooting / 故障排除

**Goal**: Fix issues  
**目标**: 解决问题

**Check / 查看**:
1. Production Guide troubleshooting section
2. Check logs: `./logs.sh all`
3. Review monitoring dashboards
4. Submit issue if needed

---

## 🔧 Configuration Files / 配置文件

### Docker Configurations / Docker 配置

| File | Purpose / 用途 |
|------|---------------|
| `docker-compose.yml` | Development environment / 开发环境 |
| `docker-compose.prod.yml` | Production environment / 生产环境 |
| `docker-compose.monitoring.yml` | Monitoring stack / 监控栈 |
| `Dockerfile` | Development image / 开发镜像 |
| `Dockerfile.prod` | Production image / 生产镜像 |

### Database Configurations / 数据库配置

| File | Purpose / 用途 |
|------|---------------|
| `docker/configs/mariadb/conf.d/custom.cnf` | MariaDB production config |
| `docker/configs/postgres/postgresql.conf` | PostgreSQL production config |

### Application Configurations / 应用配置

| File | Environment / 环境 |
|------|-------------------|
| `app/src/main/resources/application.yml` | Base / 基础 |
| `app/src/main/resources/application-docker.yml` | Development / 开发 |
| `app/src/main/resources/application-prod.yml` | Production / 生产 |

---

## 📊 Service Ports / 服务端口

### Development / 开发环境

| Service | Port | URL |
|---------|------|-----|
| Spring Boot | 8081 | http://localhost:8081/api |
| MariaDB | 3306 | localhost:3306 |
| Logto Admin | 3002 | http://localhost:3002 |
| Logto API | 3001 | http://localhost:3001 |
| PostgreSQL | 5432 | localhost:5432 |

### Production (Additional) / 生产环境（额外）

| Service | Port | URL |
|---------|------|-----|
| Grafana | 3000 | http://localhost:3000 |
| Prometheus | 9090 | http://localhost:9090 |
| Loki | 3100 | http://localhost:3100 |

---

## 📞 Support / 支持

### Documentation / 文档
- English: [docs/en/](en/)
- 中文: [docs/zh-CN/](zh-CN/)

### Issues / 问题
- GitHub Issues: [Submit Issue](your-repo-issues)

### Email / 邮箱
- Support: your-email@example.com

---

## 🔄 Updates / 更新

- **Latest Version / 最新版本**: 2.0.0
- **Last Updated / 最后更新**: 2024-12-24
- **Changelog / 更新日志**: [EN](en/CHANGELOG-PRODUCTION.md) | [CN](zh-CN/CHANGELOG-PRODUCTION.md)

---

**💡 Tip / 提示:**
Bookmark this page for quick access to all documentation!  
将本页加入书签，快速访问所有文档！

