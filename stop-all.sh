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

# Stop submodule services
echo ""
echo "🔍 停止子模块服务..."

# Auto-discover submodules from settings.gradle.kts
SUBMODULES=()
if [ -f "settings.gradle.kts" ]; then
    # Extract module names from settings.gradle.kts
    while IFS= read -r line; do
        if [[ $line =~ include\(\"([^\"]+)\"\) ]]; then
            SUBMODULES+=("${BASH_REMATCH[1]}")
        fi
    done < settings.gradle.kts
fi

# If no modules found in settings.gradle.kts, scan for directories with build.gradle.kts
if [ ${#SUBMODULES[@]} -eq 0 ]; then
    echo "   ℹ️  settings.gradle.kts not found, scanning directories..."
    for dir in */; do
        dir=${dir%/}  # Remove trailing slash
        # Skip common non-module directories
        if [[ "$dir" != "build" && "$dir" != "gradle" && "$dir" != "docker" && "$dir" != "docs" && "$dir" != "monitoring" ]]; then
            if [ -f "$dir/build.gradle.kts" ]; then
                SUBMODULES+=("$dir")
            fi
        fi
    done
fi

echo "   📋 检测到的模块: ${SUBMODULES[*]}"

STOPPED_SUBMODULES=()

for submodule in "${SUBMODULES[@]}"; do
    if [ -f "$submodule/stop.sh" ]; then
        echo "   📦 停止 $submodule/ 服务"
        chmod +x "$submodule/stop.sh"
        if [ "$REMOVE_VOLUMES" = true ]; then
            (cd "$submodule" && ./stop.sh --volumes)
        else
            (cd "$submodule" && ./stop.sh)
        fi
        STOPPED_SUBMODULES+=("$submodule")
        echo "   ✅ $submodule 服务已停止"
    fi
done

if [ ${#STOPPED_SUBMODULES[@]} -gt 0 ]; then
    echo ""
    echo "✅ 停止了 ${#STOPPED_SUBMODULES[@]} 个子模块服务: ${STOPPED_SUBMODULES[*]}"
fi

echo "✅ 所有服务已停止"

if [ "$REMOVE_VOLUMES" = true ]; then
  echo "⚠️  数据卷已删除"
fi

