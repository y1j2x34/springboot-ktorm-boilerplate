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

# Discover and start submodule services
# 发现并启动子模块服务
echo ""
echo "🔍 Scanning for submodule start scripts..."
echo "   扫描子模块启动脚本..."

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
    echo "      未找到 settings.gradle.kts，扫描目录..."
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

echo "   📋 Detected modules: ${SUBMODULES[*]}"
echo "      检测到的模块: ${SUBMODULES[*]}"

STARTED_SUBMODULES=()

for submodule in "${SUBMODULES[@]}"; do
    if [ -f "$submodule/start.sh" ]; then
        echo "   📦 Found start.sh in $submodule/"
        echo "      在 $submodule/ 中找到 start.sh"
        chmod +x "$submodule/start.sh"
        (cd "$submodule" && ./start.sh)
        STARTED_SUBMODULES+=("$submodule")
        echo "   ✅ Started $submodule services"
        echo "      $submodule 服务启动完成"
    fi
done

if [ ${#STARTED_SUBMODULES[@]} -eq 0 ]; then
    echo "   ℹ️  No submodule start scripts found"
    echo "      未找到子模块启动脚本"
else
    echo ""
    echo "✅ Started ${#STARTED_SUBMODULES[@]} submodule service(s): ${STARTED_SUBMODULES[*]}"
    echo "   启动了 ${#STARTED_SUBMODULES[@]} 个子模块服务: ${STARTED_SUBMODULES[*]}"
fi
echo ""
echo "📊 Service Status / 服务状态:"
docker compose ps

echo ""
echo "🌐 Access URLs / 访问地址:"
echo "  - Spring Boot API: http://localhost:8081/api"
echo ""
echo "📝 View Logs / 查看日志:"
echo "  ./logs.sh all                           # All services / 所有服务"
echo "  ./logs.sh springboot                    # Spring Boot only"
echo ""
echo "🛑 Stop Services / 停止服务:"
echo "  ./stop-all.sh"
echo ""
echo "📚 Documentation / 文档:"
echo "  EN: docs/en/DOCKER-QUICKSTART.md"
echo "  CN: docs/zh-CN/DOCKER-QUICKSTART.md"

