# Docker Quick Start Guide 🚀

> One-command startup for Spring Boot + Logto development environment

## 🎯 Quick Start

### Simplest Way

```bash
# 1. Start all services
./start-all.sh

# 2. Wait for services to start, then access:
#    - Spring Boot API: http://localhost:8081/api
#    - Logto Admin Panel: http://localhost:3002
```

That's it! 🎉

## 📚 Common Commands

```bash
# View logs
./logs.sh all              # All services
./logs.sh springboot       # Spring Boot only
./logs.sh logto           # Logto only

# Stop services
./stop-all.sh             # Preserve data
./stop-all.sh --volumes   # Remove all data

# Using Make (optional)
make start                # Start
make logs                 # View logs
make stop                 # Stop
make help                 # View all commands
```

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────┐
│         Docker Network (app-network)     │
│                                          │
│  ┌──────────────┐    ┌──────────────┐  │
│  │ SpringBoot   │    │   Logto      │  │
│  │     App      │◄──►│     App      │  │
│  │   :8081      │    │  :3001,3002  │  │
│  └──────┬───────┘    └──────┬───────┘  │
│         │                   │           │
│  ┌──────▼───────┐    ┌──────▼───────┐  │
│  │   MariaDB    │    │  PostgreSQL  │  │
│  │    :3306     │    │    :5432     │  │
│  └──────────────┘    └──────────────┘  │
└─────────────────────────────────────────┘
```

## 🔧 Service Details

| Service | Port | Description |
|---------|------|-------------|
| Spring Boot | 8081 | Main API application |
| MariaDB | 3306 | Application database |
| Logto App | 3001, 3002 | Authentication service |
| PostgreSQL | 5432 | Logto database |

## 💡 Design Philosophy

### Why Two docker compose Files?

✅ **Modular Management**: Logto is independent authentication service, clearer when maintained separately
✅ **Flexible Deployment**: Can choose to start only needed services
✅ **Easy Maintenance**: logto/docker-compose.yml stays original, easier to update
✅ **Network Sharing**: Services communicate through Docker network

## 📖 Detailed Documentation

See [docs/en/](../en/) or [docs/zh-CN/](../zh-CN/) for complete configuration and troubleshooting guides.

## ❓ Common Questions

**Q: First startup is slow?**  
A: Need to download Docker images and build application. Be patient. Subsequent startups will be faster.

**Q: Port already in use?**  
A: Modify port mapping in docker-compose.yml, e.g., change to `8082:8081`.

**Q: Will data be lost?**  
A: Data is stored in Docker volumes, won't be lost when stopping services. Unless using `--volumes` parameter.

**Q: How to reset all data?**  
A: Run `./stop-all.sh --volumes` or `make stop-clean`.

---

💻 Happy coding! For questions, see detailed documentation or submit an Issue.

