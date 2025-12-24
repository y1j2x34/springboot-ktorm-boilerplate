# Spring Boot Ktorm Boilerplate

Enterprise-ready Spring Boot application with Ktorm ORM, featuring complete Docker configurations for both development and production environments.

## 📖 Documentation

### Quick Links

| Language | Development | Production |
|----------|-------------|------------|
| **English** | [Quick Start](docs/en/DOCKER-QUICKSTART.md) | [Production Guide](docs/en/README.md) |
| **中文** | [快速开始](docs/zh-CN/DOCKER-QUICKSTART.md) | [生产部署](docs/zh-CN/README-PRODUCTION.md) |

### Complete Documentation

- **English**: [docs/en/](docs/en/)
- **中文**: [docs/zh-CN/](docs/zh-CN/)

## 🚀 Quick Start

### Development Environment

```bash
# Start all services
./start-all.sh

# Access:
# - API: http://localhost:8081/api
# - Logto: http://localhost:3002
```

### Production Environment

```bash
# 1. Configure environment
cp env.prod.example .env.prod
vim .env.prod

# 2. Deploy
./deploy-prod.sh --with-monitoring
```

## 📦 Features

### Development
- ✅ One-command startup
- ✅ Hot reload support
- ✅ Easy debugging
- ✅ Docker-based development

### Production
- ✅ Security hardening (non-root user, network isolation)
- ✅ Resource management (CPU/memory limits)
- ✅ Complete monitoring (Prometheus + Grafana + Loki)
- ✅ Automated deployment scripts
- ✅ Health checks and auto-restart
- ✅ Log management and rotation

## 🏗️ Project Structure

```
.
├── docs/
│   ├── en/                      # English documentation
│   └── zh-CN/                   # Chinese documentation
├── docker/
│   └── configs/                 # Database configurations
│       ├── mariadb/
│       └── postgres/
├── app/                         # Main application
├── common/                      # Common modules
├── captcha/                     # Captcha module
├── jwt-auth/                    # JWT authentication
├── user/                        # User module
├── logto/                       # Logto authentication service
├── monitoring/                  # Monitoring configurations
├── docker-compose.yml           # Development environment
├── docker-compose.prod.yml      # Production environment
├── docker-compose.monitoring.yml # Monitoring stack
└── deploy-prod.sh               # Deployment script
```

## 🛠️ Tech Stack

- **Backend**: Spring Boot 2.7.1, Kotlin 1.8.20
- **ORM**: Ktorm 3.6.0
- **Database**: MariaDB 11.2
- **Auth**: Logto, JWT
- **Captcha**: Anji-Plus Captcha
- **Containerization**: Docker, Docker Compose
- **Monitoring**: Prometheus, Grafana, Loki

## 📊 Services

### Development Environment

| Service | Port | Description |
|---------|------|-------------|
| Spring Boot | 8081 | Main API |
| MariaDB | 3306 | Database |
| Logto | 3001, 3002 | Authentication |
| PostgreSQL | 5432 | Logto database |

### Production Environment (Additional)

| Service | Port | Description |
|---------|------|-------------|
| Grafana | 3000 | Monitoring dashboards |
| Prometheus | 9090 | Metrics collection |
| Loki | 3100 | Log aggregation |

## 🔒 Security Features (Production)

- Non-root user execution
- Network isolation (internal/external networks)
- Environment variable management for secrets
- Security options (no-new-privileges)
- SSL/TLS support
- Regular security updates

## 📈 Monitoring (Production)

- Real-time metrics (CPU, Memory, Requests)
- Application performance monitoring
- Log aggregation and search
- Pre-configured alerts (20+ rules)
- Grafana dashboards
- Health checks

## 💾 Backup & Recovery (Production)

- Automated database backups
- Volume backup scripts
- Point-in-time recovery
- Backup retention policies
- Disaster recovery procedures

## 🎯 Use Cases

### Development
```bash
./start-all.sh          # Start
./logs.sh all           # View logs
./stop-all.sh          # Stop
make help              # View all commands
```

### Production
```bash
./deploy-prod.sh                    # Deploy
./deploy-prod.sh --with-monitoring  # Deploy with monitoring
./stop-prod.sh                      # Stop
./stop-prod.sh --volumes            # Stop and remove data
```

## 📚 Documentation Index

### Getting Started
- [Development Quick Start (EN)](docs/en/DOCKER-QUICKSTART.md) | [开发快速开始 (CN)](docs/zh-CN/DOCKER-QUICKSTART.md)
- [Production Quick Start (EN)](docs/en/README.md) | [生产快速开始 (CN)](docs/zh-CN/README-PRODUCTION.md)

### Complete Guides
- [Production Deployment Guide (EN)](docs/en/PRODUCTION.md) | [生产部署指南 (CN)](docs/zh-CN/PRODUCTION.md)
- [Production Checklist (EN)](docs/en/PRODUCTION-CHECKLIST.md) | [生产检查清单 (CN)](docs/zh-CN/PRODUCTION-CHECKLIST.md)

### Reference
- [Development vs Production (EN)](docs/en/DEV-VS-PROD.md) | [开发vs生产 (CN)](docs/zh-CN/DEV-VS-PROD.md)
- [Documentation Index (EN)](docs/en/INDEX.md) | [文档索引 (CN)](docs/zh-CN/INDEX.md)

## 🆘 Troubleshooting

### Service won't start
```bash
# Development
./logs.sh all

# Production
docker compose -f docker-compose.prod.yml logs
```

### Port conflicts
Edit port mappings in `docker-compose.yml` or `docker-compose.prod.yml`

### Database connection issues
```bash
# Check database status
docker compose ps
docker exec mariadb-db mysql -uroot -p -e "SELECT 1"
```

## 🤝 Contributing

Contributions are welcome! Please read our contributing guidelines before submitting pull requests.

## 📝 License

[Your License]

## 📞 Support

- Documentation: [docs/](docs/)
- Issues: [GitHub Issues](your-repo-issues)
- Email: your-email@example.com

---

**⚠️ Important Notes:**
- For production deployment, read the complete [Production Guide](docs/en/PRODUCTION.md)
- Always use strong passwords in production
- Enable monitoring for production environments
- Backup data regularly
- Test thoroughly before production deployment

