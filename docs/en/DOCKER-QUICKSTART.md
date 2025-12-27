# Docker Quick Start Guide 🚀

> One-command startup for Spring Boot development environment

## 🎯 Quick Start

### Simplest Way

```bash
# 1. Start all services
./start-all.sh

# 2. Wait for services to start, then access:
#    - Spring Boot API: http://localhost:8081/api
```

That's it! 🎉

## 📚 Common Commands

```bash
# View logs
./logs.sh all              # All services
./logs.sh springboot       # Spring Boot only

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
│  ┌──────────────┐                        │
│  │ SpringBoot   │                        │
│  │     App      │                        │
│  │   :8081      │                        │
│  └──────┬───────┘                        │
│         │                                │
│  ┌──────▼───────┐                        │
│  │   MariaDB    │                        │
│  │    :3306     │                        │
│  └──────────────┘                        │
└─────────────────────────────────────────┘
```

## 🔧 Service Details

| Service | Port | Description |
|---------|------|-------------|
| Spring Boot | 8081 | Main API application |
| MariaDB | 3306 | Application database |

## 💡 Design Philosophy

✅ **Modular Management**: Clearer when maintained separately
✅ **Flexible Deployment**: Can choose to start only needed services
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

