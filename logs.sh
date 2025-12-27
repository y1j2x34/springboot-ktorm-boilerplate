#!/bin/bash

# 查看日志的脚本
# 使用方式: ./logs.sh [service]
# 服务选项: springboot, all (默认)

SERVICE=${1:-all}

case $SERVICE in
  springboot)
    echo "📋 查看 Spring Boot 应用日志..."
    docker compose logs -f springboot-app
    ;;
  all)
    echo "📋 查看所有服务日志..."
    echo "提示: 使用 Ctrl+C 退出"
    echo ""
    docker compose logs -f
    ;;
  *)
    echo "未知服务: $SERVICE"
    echo "使用方式: ./logs.sh [springboot|all]"
    exit 1
    ;;
esac

