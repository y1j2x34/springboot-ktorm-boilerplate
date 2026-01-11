#!/bin/bash

# Stop database and Redis services for local development debugging

set -e

echo "🛑 Stopping debug services..."

docker compose -f docker-compose.debug.yml down

echo ""
echo "✅ Debug services stopped successfully!"

