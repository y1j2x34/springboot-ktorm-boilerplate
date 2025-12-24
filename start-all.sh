#!/bin/bash

# 启动所有服务的脚本
# 使用方式: ./start-all.sh

set -e

echo "🚀 Starting Spring Boot application and databases..."
echo "   启动 Spring Boot 应用和数据库..."

# Start main docker compose first (creates network)
# 首先启动主 docker-compose（创建网络）
docker compose up -d

echo "✅ Spring Boot application started successfully"
echo "   Spring Boot 应用启动完成"
echo ""
echo "🔐 Starting Logto authentication service..."
echo "   启动 Logto 认证服务..."

# Start logto services in logto directory
# 然后在 logto 目录启动 logto 服务
cd logto
docker compose up -d
cd ..

echo "✅ Logto service started successfully"
echo "   Logto 服务启动完成"
echo ""
echo "📊 Service Status / 服务状态:"
docker compose ps
cd logto && docker compose ps && cd ..

echo ""
echo "🌐 Access URLs / 访问地址:"
echo "  - Spring Boot API: http://localhost:8081/api"
echo "  - Logto Admin Panel / Logto 管理界面: http://localhost:3002"
echo "  - Logto API: http://localhost:3001"
echo ""
echo "📝 View Logs / 查看日志:"
echo "  ./logs.sh all                           # All services / 所有服务"
echo "  ./logs.sh springboot                    # Spring Boot only"
echo "  ./logs.sh logto                         # Logto only"
echo ""
echo "🛑 Stop Services / 停止服务:"
echo "  ./stop-all.sh"
echo ""
echo "📚 Documentation / 文档:"
echo "  EN: docs/en/DOCKER-QUICKSTART.md"
echo "  CN: docs/zh-CN/DOCKER-QUICKSTART.md"

