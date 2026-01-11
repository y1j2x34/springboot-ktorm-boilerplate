#!/bin/sh
set -e

# 如果设置了密码，则使用密码启动 Redis
if [ -n "$REDIS_PASSWORD" ]; then
    exec redis-server --appendonly yes --requirepass "$REDIS_PASSWORD"
else
    exec redis-server --appendonly yes
fi

