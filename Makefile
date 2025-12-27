.PHONY: help start stop restart logs logs-springboot build clean status

# 默认目标
help:
	@echo "📦 SpringBoot Ktorm Boilerplate - Docker 管理命令"
	@echo ""
	@echo "使用方式:"
	@echo "  make start              - 启动所有服务"
	@echo "  make stop               - 停止所有服务"
	@echo "  make stop-clean         - 停止所有服务并删除数据卷"
	@echo "  make restart            - 重启所有服务"
	@echo "  make logs               - 查看所有日志"
	@echo "  make logs-springboot    - 查看 Spring Boot 日志"
	@echo "  make build              - 重新构建 Spring Boot 镜像"
	@echo "  make status             - 查看服务状态"
	@echo "  make clean              - 清理所有容器和数据卷"
	@echo ""

# 启动所有服务
start:
	@echo "🚀 启动 Spring Boot 应用..."
	@docker compose up -d
	@echo "✅ 所有服务已启动"
	@make status

# 停止所有服务
stop:
	@echo "🛑 停止所有服务..."
	@docker compose down
	@echo "✅ 所有服务已停止"

# 停止并删除数据卷
stop-clean:
	@echo "🛑 停止所有服务并删除数据卷..."
	@docker compose down -v
	@echo "✅ 所有服务已停止，数据卷已删除"

# 重启所有服务
restart: stop start

# 查看所有日志
logs:
	@echo "📋 查看所有服务日志 (Ctrl+C 退出)..."
	@docker compose logs -f

# 查看 Spring Boot 日志
logs-springboot:
	@echo "📋 查看 Spring Boot 日志..."
	@docker compose logs -f springboot-app

# 重新构建 Spring Boot 镜像
build:
	@echo "🔨 重新构建 Spring Boot 镜像..."
	@docker compose build --no-cache springboot-app
	@echo "✅ 构建完成"

# 查看服务状态
status:
	@echo ""
	@echo "📊 Spring Boot 服务状态:"
	@docker compose ps
	@echo ""
	@echo "🌐 访问地址:"
	@echo "  - Spring Boot API: http://localhost:8081/api"

# 清理所有容器、镜像和数据卷
clean: stop
	@echo "🧹 清理 Docker 资源..."
	@docker system prune -f
	@echo "✅ 清理完成"

