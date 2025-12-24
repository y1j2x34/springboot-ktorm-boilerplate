#!/bin/bash

# Production Deployment Script
# Usage: ./deploy-prod.sh [options]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
WITH_MONITORING=false
ENV_FILE=".env.prod"
DATA_DIR="/var/lib/springboot-ktorm-app"

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --with-monitoring)
      WITH_MONITORING=true
      shift
      ;;
    --env-file)
      ENV_FILE="$2"
      shift 2
      ;;
    --data-dir)
      DATA_DIR="$2"
      shift 2
      ;;
    -h|--help)
      echo "Usage: $0 [options]"
      echo ""
      echo "Options:"
      echo "  --with-monitoring    Deploy with monitoring stack"
      echo "  --env-file FILE      Use specific env file (default: .env.prod)"
      echo "  --data-dir DIR       Data directory (default: /var/lib/springboot-ktorm-app)"
      echo "  -h, --help          Show this help message"
      exit 0
      ;;
    *)
      echo -e "${RED}Unknown option: $1${NC}"
      exit 1
      ;;
  esac
done

echo -e "${GREEN}╔════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║   Production Deployment - Spring Boot Ktorm   ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════╝${NC}"
echo ""

# Check if env file exists
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}✗ Error: Environment file $ENV_FILE not found!${NC}"
    echo -e "${YELLOW}Please create it from env.prod.example:${NC}"
    echo -e "${YELLOW}  cp env.prod.example .env.prod${NC}"
    echo -e "${YELLOW}  vim .env.prod  # Edit with your values${NC}"
    echo -e "${YELLOW}  chmod 600 .env.prod${NC}"
    echo -e ""
    echo -e "${YELLOW}Refer to: docs/en/PRODUCTION.md or docs/zh-CN/PRODUCTION.md${NC}"
    exit 1
fi

echo -e "${GREEN}✓${NC} Using environment file: $ENV_FILE"

# Load environment variables
export $(cat "$ENV_FILE" | grep -v '^#' | xargs)

# Create data directories
echo ""
echo -e "${YELLOW}Creating data directories...${NC}"
mkdir -p "$DATA_DIR/mariadb"
mkdir -p "$DATA_DIR/postgres"
chmod 755 "$DATA_DIR"
chmod 700 "$DATA_DIR/mariadb"
chmod 700 "$DATA_DIR/postgres"
echo -e "${GREEN}✓${NC} Data directories created"

# Pre-deployment checks
echo ""
echo -e "${YELLOW}Running pre-deployment checks...${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}✗ Docker is not running!${NC}"
    exit 1
fi
echo -e "${GREEN}✓${NC} Docker is running"

# Check if required environment variables are set
required_vars=("DB_PASSWORD" "DB_ROOT_PASSWORD" "VERSION")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo -e "${RED}✗ Required environment variable $var is not set!${NC}"
        exit 1
    fi
done
echo -e "${GREEN}✓${NC} Required environment variables are set"

# Build the application
echo ""
echo -e "${YELLOW}Building Spring Boot application...${NC}"
export BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
docker compose -f docker-compose.prod.yml build --no-cache
echo -e "${GREEN}✓${NC} Application built successfully"

# Stop existing containers
echo ""
echo -e "${YELLOW}Stopping existing containers...${NC}"
docker compose -f docker-compose.prod.yml down
cd logto && docker compose -f docker-compose.prod.yml down && cd ..
echo -e "${GREEN}✓${NC} Existing containers stopped"

# Start the main application
echo ""
echo -e "${YELLOW}Starting main application...${NC}"
docker compose -f docker-compose.prod.yml up -d

# Wait for application to be healthy
echo ""
echo -e "${YELLOW}Waiting for application to be healthy...${NC}"
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if docker compose -f docker-compose.prod.yml ps | grep -q "healthy"; then
        echo -e "${GREEN}✓${NC} Application is healthy"
        break
    fi
    attempt=$((attempt + 1))
    echo -n "."
    sleep 2
done
echo ""

if [ $attempt -eq $max_attempts ]; then
    echo -e "${RED}✗ Application failed to become healthy${NC}"
    docker compose -f docker-compose.prod.yml logs --tail=50
    exit 1
fi

# Start Logto
echo ""
echo -e "${YELLOW}Starting Logto services...${NC}"
cd logto
docker compose -f docker-compose.prod.yml up -d
cd ..
echo -e "${GREEN}✓${NC} Logto services started"

# Start monitoring if requested
if [ "$WITH_MONITORING" = true ]; then
    echo ""
    echo -e "${YELLOW}Starting monitoring stack...${NC}"
    docker compose -f docker-compose.prod.yml -f docker-compose.monitoring.yml up -d
    echo -e "${GREEN}✓${NC} Monitoring stack started"
fi

# Show status
echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║          Deployment Completed Successfully     ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Service Status:${NC}"
docker compose -f docker-compose.prod.yml ps
echo ""
cd logto && docker compose -f docker-compose.prod.yml ps && cd ..
echo ""

if [ "$WITH_MONITORING" = true ]; then
    echo -e "${YELLOW}Monitoring Stack:${NC}"
    docker compose -f docker-compose.monitoring.yml ps
    echo ""
fi

echo -e "${YELLOW}Access URLs:${NC}"
echo "  - Spring Boot API: http://localhost:${APP_PORT:-8081}/api"
echo "  - Logto Admin: ${ADMIN_ENDPOINT}"
echo "  - Logto API: ${ENDPOINT}"

if [ "$WITH_MONITORING" = true ]; then
    echo "  - Grafana: http://localhost:3000"
    echo "  - Prometheus: http://localhost:9090"
fi

echo ""
echo -e "${YELLOW}View logs:${NC}"
echo "  docker compose -f docker-compose.prod.yml logs -f"
echo ""
echo -e "${YELLOW}Stop services:${NC}"
echo "  ./stop-prod.sh"
echo ""

