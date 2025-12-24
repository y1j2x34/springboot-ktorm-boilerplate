# Production Deployment Guide

Complete guide for deploying Spring Boot Ktorm application to production environment.

## 📋 Table of Contents

- [System Requirements](#system-requirements)
- [Pre-deployment Preparation](#pre-deployment-preparation)
- [Quick Deployment](#quick-deployment)
- [Detailed Configuration](#detailed-configuration)
- [Monitoring and Logging](#monitoring-and-logging)
- [Security Hardening](#security-hardening)
- [Backup and Recovery](#backup-and-recovery)
- [Troubleshooting](#troubleshooting)

## System Requirements

### Hardware Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| CPU | 2 cores | 4+ cores |
| Memory | 4 GB | 8+ GB |
| Disk | 20 GB | 50+ GB SSD |
| Network | 100 Mbps | 1 Gbps |

### Software Requirements

- **Operating System**: Linux (Ubuntu 20.04+, CentOS 8+, Debian 11+)
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **System Tools**: curl, wget, git

## Pre-deployment Preparation

### 1. Install Docker and Docker Compose

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 2. Clone Repository

```bash
git clone <your-repository>
cd springboot-ktorm-boilerplate
```

### 3. Configure Environment Variables

```bash
# Create production environment file
cp env.prod.example .env.prod

# Edit configuration (IMPORTANT: Change all passwords!)
vim .env.prod

# Secure the file
chmod 600 .env.prod
```

**Security Guidelines:**
- ✅ Use strong passwords (16+ characters minimum)
- ✅ Use different passwords for different services
- ✅ Never commit `.env.prod` to version control
- ✅ Rotate passwords regularly

### 4. Create Data Directories

```bash
sudo mkdir -p /var/lib/springboot-ktorm-app/{mariadb,postgres}
sudo chown -R $USER:$USER /var/lib/springboot-ktorm-app
chmod 700 /var/lib/springboot-ktorm-app/{mariadb,postgres}
```

## Quick Deployment

### Method 1: Using Deployment Script (Recommended)

```bash
# Basic deployment
./deploy-prod.sh

# With monitoring stack
./deploy-prod.sh --with-monitoring

# Custom environment file
./deploy-prod.sh --env-file .env.custom
```

### Method 2: Manual Deployment

```bash
# 1. Build images
docker compose -f docker-compose.prod.yml build

# 2. Start services
docker compose -f docker-compose.prod.yml up -d

# 3. Start Logto
cd logto && docker compose -f docker-compose.prod.yml up -d && cd ..

# 4. (Optional) Start monitoring
docker compose -f docker-compose.prod.yml -f docker-compose.monitoring.yml up -d
```

## Detailed Configuration

### Resource Limits

Each service has resource limits configured in `docker-compose.prod.yml`:

```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'        # Maximum CPU cores
      memory: 2560M      # Maximum memory
    reservations:
      cpus: '0.5'        # Reserved CPU
      memory: 512M       # Reserved memory
```

### Database Optimization

#### MariaDB Configuration

Edit `docker/configs/mariadb/conf.d/custom.cnf`:

```ini
# Adjust based on available memory
innodb_buffer_pool_size = 1G  # Recommended: 70-80% of available RAM
max_connections = 500          # Adjust based on concurrency
```

#### PostgreSQL Configuration

Edit `docker/configs/postgres/postgresql.conf`:

```ini
shared_buffers = 256MB         # Recommended: 25% of RAM
effective_cache_size = 1GB     # Recommended: 50-75% of RAM
max_connections = 200
```

### JVM Tuning

Adjust `JAVA_OPTS` in `docker-compose.prod.yml`:

```yaml
environment:
  - JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

**Recommended Settings:**
- `-Xms`: Initial heap size (50% of max heap)
- `-Xmx`: Maximum heap size (no more than 75% of container memory)
- `UseG1GC`: Recommended garbage collector

## Monitoring and Logging

### Start Monitoring Stack

```bash
# Deploy with monitoring
./deploy-prod.sh --with-monitoring

# Or start separately
docker compose -f docker-compose.monitoring.yml up -d
```

### Monitoring Components

| Service | Port | Description |
|---------|------|-------------|
| Grafana | 3000 | Visualization dashboards |
| Prometheus | 9090 | Metrics collection |
| Loki | 3100 | Log aggregation |
| cAdvisor | 8080 | Container monitoring |

### Access Grafana

```bash
# Default credentials
URL: http://your-server:3000
Username: admin
Password: admin (change on first login)
```

### View Logs

```bash
# Application logs
docker compose -f docker-compose.prod.yml logs -f springboot-app

# Database logs
docker compose -f docker-compose.prod.yml logs -f mariadb

# All service logs
docker compose -f docker-compose.prod.yml logs -f

# Export logs
docker compose -f docker-compose.prod.yml logs --no-color > app.log
```

### Configure Alerts

Edit `monitoring/prometheus/alerts.yml` to add custom alert rules.

## Security Hardening

### 1. Firewall Configuration

```bash
# UFW (Ubuntu)
sudo ufw allow 22/tcp      # SSH
sudo ufw allow 80/tcp      # HTTP
sudo ufw allow 443/tcp     # HTTPS
sudo ufw allow 8081/tcp    # Spring Boot (if external access needed)
sudo ufw enable

# Restrict database access to internal network only
sudo ufw allow from 192.168.1.0/24 to any port 3306
```

### 2. SSL/TLS Configuration

Use Nginx or Traefik as reverse proxy with SSL certificate:

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

### 3. Network Isolation

Use internal networks in `docker-compose.prod.yml`:

```yaml
networks:
  internal-network:
    driver: bridge
    internal: true  # Isolate from external access
```

### 4. Regular Updates

```bash
# Update Docker images
docker compose -f docker-compose.prod.yml pull

# Redeploy
./deploy-prod.sh
```

## Backup and Recovery

### Database Backup

#### MariaDB Backup

```bash
# Create backup directory
mkdir -p /backup/mariadb

# Backup script
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

# Keep last 7 days of backups
find ${BACKUP_DIR} -name "backup_*.sql.gz" -mtime +7 -delete
EOF

chmod +x /backup/backup-mariadb.sh

# Setup cron job (daily at 2:00 AM)
echo "0 2 * * * /backup/backup-mariadb.sh" | crontab -
```

#### PostgreSQL Backup

```bash
# Create backup directory
mkdir -p /backup/postgres

# Backup script
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

# Keep last 7 days of backups
find ${BACKUP_DIR} -name "backup_*.dump.gz" -mtime +7 -delete
EOF

chmod +x /backup/backup-postgres.sh

# Setup cron job
echo "0 2 * * * /backup/backup-postgres.sh" | crontab -
```

### Restore Data

#### MariaDB Restore

```bash
# Restore backup
gunzip < /backup/mariadb/backup_20241224_020000.sql.gz | \
  docker exec -i mariadb-db mysql -uroot -pYOUR_PASSWORD web_ai
```

#### PostgreSQL Restore

```bash
# Restore backup
gunzip < /backup/postgres/backup_20241224_020000.dump.gz | \
  docker exec -i postgres-db pg_restore -U postgres -d logto --clean
```

### Volume Backup

```bash
# Stop services
./stop-prod.sh

# Backup data directory
tar -czf /backup/volumes_$(date +%Y%m%d).tar.gz /var/lib/springboot-ktorm-app

# Restart services
./deploy-prod.sh
```

## Troubleshooting

### Service Won't Start

```bash
# Check logs
docker compose -f docker-compose.prod.yml logs

# Check container status
docker compose -f docker-compose.prod.yml ps

# Check resource usage
docker stats

# Check disk space
df -h
```

### Performance Issues

```bash
# Check database connections
docker exec mariadb-db mysqladmin -uroot -p processlist

# Check slow query log
docker exec mariadb-db tail -f /var/log/mysql/slow-query.log

# Check JVM heap usage
docker exec springboot-ktorm-app jstat -gc 1 1000
```

### Out of Memory Errors

```bash
# Check heap dumps
docker exec springboot-ktorm-app ls -lh /app/logs/

# Download heap dump for analysis
docker cp springboot-ktorm-app:/app/logs/heap-dump.hprof ./
```

### Network Issues

```bash
# Check networks
docker network ls
docker network inspect springboot-ktorm-boilerplate_app-network

# Test connectivity
docker exec springboot-ktorm-app ping mariadb
docker exec springboot-ktorm-app curl http://localhost:8081/api/actuator/health
```

## Upgrade and Rollback

### Upgrade Application

```bash
# 1. Backup data
./stop-prod.sh
tar -czf backup_before_upgrade.tar.gz /var/lib/springboot-ktorm-app

# 2. Pull latest code
git pull origin main

# 3. Update version
export VERSION=1.1.0

# 4. Redeploy
./deploy-prod.sh
```

### Rollback

```bash
# 1. Stop services
./stop-prod.sh

# 2. Switch to old version
git checkout v1.0.0

# 3. Restore data (if needed)
tar -xzf backup_before_upgrade.tar.gz -C /

# 4. Redeploy
./deploy-prod.sh
```

## Performance Tuning Recommendations

### 1. Database Tuning

- Regularly run `ANALYZE TABLE` to update statistics
- Monitor slow query log and optimize SQL
- Adjust connection pool size appropriately
- Enable query cache (for suitable scenarios)

### 2. Application Tuning

- Enable HTTP/2
- Configure response compression
- Use CDN for static assets
- Implement caching strategy (Redis)

### 3. System Tuning

```bash
# Increase file descriptor limit
echo "* soft nofile 65536" >> /etc/security/limits.conf
echo "* hard nofile 65536" >> /etc/security/limits.conf

# Optimize TCP parameters
cat >> /etc/sysctl.conf << EOF
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_fin_timeout = 30
net.core.somaxconn = 1024
EOF

sysctl -p
```

## Maintenance Schedule

### Daily Maintenance

- ✅ Check service status
- ✅ Review error logs
- ✅ Monitor resource usage
- ✅ Check disk space

### Weekly Maintenance

- ✅ Verify backup integrity
- ✅ Review slow query logs
- ✅ Update security patches
- ✅ Check alert configuration

### Monthly Maintenance

- ✅ Performance testing and optimization
- ✅ Database optimization
- ✅ Security audit
- ✅ Documentation updates

## Support

Having issues?

1. Check logs: `docker compose -f docker-compose.prod.yml logs`
2. Verify health: `curl http://localhost:8081/api/actuator/health`
3. Review monitoring dashboards: Grafana
4. Refer to checklist: [PRODUCTION-CHECKLIST.md](PRODUCTION-CHECKLIST.md)
5. Submit issue: [GitHub Issues](your-repo-issues)

---

**Important Reminders:**
- Thoroughly test before production deployment
- Backup data regularly
- Monitor system status
- Update security patches promptly
- Protect sensitive information

