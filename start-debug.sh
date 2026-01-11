#!/bin/bash

# Start database and Redis services for local development debugging
# The Spring Boot application should be started via IntelliJ IDEA

set -e

echo "🚀 Starting debug services (database and Redis)..."
echo ""

docker compose -f docker-compose.debug.yml up -d

echo ""
echo "✅ Debug services started successfully!"
echo ""
echo "Services:"
echo "  - MariaDB: localhost:3306"
echo "  - Redis:   localhost:6379"
echo ""
echo "Next steps:"
echo "  1. Start your Spring Boot application in IntelliJ IDEA"
echo "  2. Configure environment variables (see DEBUG.md)"
echo ""
echo "To stop services:"
echo "  ./stop-debug.sh"
echo ""
echo "To view logs:"
echo "  docker compose -f docker-compose.debug.yml logs -f"

