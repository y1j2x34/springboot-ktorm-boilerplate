# Production Deployment - Quick Reference

## 🚀 Quick Start

### 1. Prepare Environment File

```bash
cp env.prod.example .env.prod
# Edit .env.prod with your production values
chmod 600 .env.prod
```

### 2. Deploy

```bash
# Basic deployment
./deploy-prod.sh

# With monitoring
./deploy-prod.sh --with-monitoring
```

### 3. Verify

```bash
# Check health
curl http://localhost:8081/actuator/health

# View logs
docker compose -f docker-compose.prod.yml logs -f
```

## 📊 Service Ports

| Service | Port | Access URL |
|---------|------|------------|
| Spring Boot API | 8081 | http://localhost:8081/api |
| Grafana | 3000 | http://localhost:3000 |
| Prometheus | 9090 | http://localhost:9090 |

## 🔒 Security Requirements

- ✅ Strong passwords (16+ characters)
- ✅ Firewall configured
- ✅ SSL/TLS enabled
- ✅ Regular security updates
- ✅ Backup strategy in place

## 📦 Project Structure

```
.
├── docs/
│   ├── en/              # English documentation
│   └── zh-CN/           # Chinese documentation
├── docker/
│   └── configs/         # Database configurations
│       └── mariadb/
├── docker-compose.prod.yml      # Production main services
├── docker-compose.monitoring.yml # Monitoring stack
├── Dockerfile.prod              # Production Dockerfile
├── deploy-prod.sh               # Deployment script
└── stop-prod.sh                 # Stop script
```

## 🛠️ Common Commands

```bash
# Deploy
./deploy-prod.sh

# Stop
./stop-prod.sh

# Stop and remove data
./stop-prod.sh --volumes

# View logs
docker compose -f docker-compose.prod.yml logs -f springboot-app

# Check status
docker compose -f docker-compose.prod.yml ps

# Restart service
docker compose -f docker-compose.prod.yml restart springboot-app
```

## 💾 Backup

```bash
# Database backup
/backup/backup-mariadb.sh

# Volume backup
tar -czf backup.tar.gz /var/lib/springboot-ktorm-app
```

## 📈 Monitoring

Access Grafana at `http://localhost:3000`:
- Default credentials: admin/admin (change on first login)
- Pre-configured dashboards for Spring Boot metrics
- Real-time log viewing via Loki

## 🔧 Resource Limits

| Service | CPU | Memory |
|---------|-----|--------|
| Spring Boot | 2 cores | 2.5 GB |
| MariaDB | 2 cores | 2 GB |

## 📚 Documentation

- **[PRODUCTION.md](PRODUCTION.md)** - Complete production guide
- **[PRODUCTION-CHECKLIST.md](PRODUCTION-CHECKLIST.md)** - Deployment checklist
- **[../zh-CN/](../zh-CN/)** - Chinese documentation

## 🆘 Troubleshooting

### Service won't start
```bash
docker compose -f docker-compose.prod.yml logs
```

### High memory usage
```bash
docker stats
```

### Database connection issues
```bash
docker exec mariadb-db mysql -uroot -p -e "SHOW PROCESSLIST;"
```

### Network issues
```bash
docker network inspect springboot-ktorm-boilerplate_app-network
```

## 🔄 Update & Rollback

### Update
```bash
git pull origin main
./deploy-prod.sh
```

### Rollback
```bash
./stop-prod.sh
git checkout previous-version
./deploy-prod.sh
```

## 📞 Support

For detailed information, refer to:
- [PRODUCTION.md](PRODUCTION.md) - Complete deployment guide
- [PRODUCTION-CHECKLIST.md](PRODUCTION-CHECKLIST.md) - Pre-deployment checklist

---

**⚠️ Important:**
- Always backup before deployment
- Test in staging environment first
- Monitor system after deployment
- Keep credentials secure
- Document all changes

