#!/bin/bash

# Production Stop Script
# Usage: ./stop-prod.sh [options]

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Default values
REMOVE_VOLUMES=false
REMOVE_MONITORING=false

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    -v|--volumes)
      REMOVE_VOLUMES=true
      shift
      ;;
    --monitoring)
      REMOVE_MONITORING=true
      shift
      ;;
    -h|--help)
      echo "Usage: $0 [options]"
      echo ""
      echo "Options:"
      echo "  -v, --volumes       Remove data volumes"
      echo "  --monitoring        Stop monitoring stack"
      echo "  -h, --help         Show this help message"
      exit 0
      ;;
    *)
      echo -e "${RED}Unknown option: $1${NC}"
      exit 1
      ;;
  esac
done

echo -e "${YELLOW}Stopping production services...${NC}"

# Stop monitoring if requested
if [ "$REMOVE_MONITORING" = true ]; then
    echo -e "${YELLOW}Stopping monitoring stack...${NC}"
    if [ "$REMOVE_VOLUMES" = true ]; then
        docker compose -f docker-compose.monitoring.yml down -v
    else
        docker compose -f docker-compose.monitoring.yml down
    fi
    echo -e "${GREEN}✓${NC} Monitoring stack stopped"
fi

# Stop Logto
echo -e "${YELLOW}Stopping Logto services...${NC}"
cd logto
if [ "$REMOVE_VOLUMES" = true ]; then
    docker compose -f docker-compose.prod.yml down -v
else
    docker compose -f docker-compose.prod.yml down
fi
cd ..
echo -e "${GREEN}✓${NC} Logto services stopped"

# Stop main application
echo -e "${YELLOW}Stopping main application...${NC}"
if [ "$REMOVE_VOLUMES" = true ]; then
    docker compose -f docker-compose.prod.yml down -v
else
    docker compose -f docker-compose.prod.yml down
fi
echo -e "${GREEN}✓${NC} Main application stopped"

if [ "$REMOVE_VOLUMES" = true ]; then
    echo -e "${RED}⚠️  Data volumes have been removed${NC}"
fi

echo -e "${GREEN}✓${NC} All services stopped successfully"

