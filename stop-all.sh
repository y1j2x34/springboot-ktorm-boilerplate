#!/bin/bash

# 停止所有服务的脚本
# 使用方式: ./stop-all.sh [options]
# 选项:
#   -v, --volumes    同时删除数据卷

set -e

REMOVE_VOLUMES=false

# 解析命令行参数
while [[ $# -gt 0 ]]; do
  case $1 in
    -v|--volumes)
      REMOVE_VOLUMES=true
      shift
      ;;
    *)
      echo "未知选项: $1"
      echo "使用方式: ./stop-all.sh [-v|--volumes]"
      exit 1
      ;;
  esac
done

echo "🛑 停止 Spring Boot 应用..."
if [ "$REMOVE_VOLUMES" = true ]; then
  docker compose down -v
else
  docker compose down
fi

echo "✅ 所有服务已停止"

if [ "$REMOVE_VOLUMES" = true ]; then
  echo "⚠️  数据卷已删除"
fi

