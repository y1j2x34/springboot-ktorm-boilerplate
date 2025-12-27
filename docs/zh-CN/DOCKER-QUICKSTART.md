# Docker 快速开始指南 🚀

> 一键启动 Spring Boot 开发环境

## 🎯 快速启动

### 最简单的方式

```bash
# 1. 启动所有服务
./start-all.sh

# 2. 等待服务启动完成，然后访问：
#    - Spring Boot API: http://localhost:8081/api
```

就这么简单！🎉

## 📚 其他常用命令

```bash
# 查看日志
./logs.sh all              # 所有服务
./logs.sh springboot       # 仅 Spring Boot

# 停止服务
./stop-all.sh             # 保留数据
./stop-all.sh --volumes   # 删除所有数据

# 使用 Make（可选）
make start                # 启动
make logs                 # 查看日志
make stop                 # 停止
make help                 # 查看所有命令
```

## 🏗️ 架构概览

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

## 🔧 服务说明

| 服务 | 端口 | 说明 |
|------|------|------|
| Spring Boot | 8081 | 主应用 API |
| MariaDB | 3306 | 应用数据库 |

## 💡 设计理念

✅ **模块化管理**: 模块化设计，单独维护更清晰  
✅ **灵活部署**: 可以选择只启动需要的服务  
✅ **网络共享**: 通过 Docker 网络实现服务间通信  

## 📖 详细文档

查看 [DOCKER.md](DOCKER.md) 获取完整的配置和故障排除指南。

## ❓ 常见问题

**Q: 第一次启动很慢？**  
A: 需要下载 Docker 镜像和构建应用，请耐心等待。后续启动会很快。

**Q: 端口被占用？**  
A: 修改 docker-compose.yml 中的端口映射，例如改为 `8082:8081`。

**Q: 数据会丢失吗？**  
A: 数据存储在 Docker 卷中，停止服务不会丢失。除非使用 `--volumes` 参数。

**Q: 如何重置所有数据？**  
A: 运行 `./stop-all.sh --volumes` 或 `make stop-clean`。

---

💻 开发愉快！如有问题请查看 [DOCKER.md](DOCKER.md) 或提交 Issue。

