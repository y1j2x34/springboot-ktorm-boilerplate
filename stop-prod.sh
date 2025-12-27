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

# Stop main application
echo -e "${YELLOW}Stopping main application...${NC}"
if [ "$REMOVE_VOLUMES" = true ]; then
    docker compose -f docker-compose.prod.yml down -v
else
    docker compose -f docker-compose.prod.yml down
fi
echo -e "${GREEN}✓${NC} Main application stopped"

# Stop submodule services
echo ""
echo -e "${YELLOW}Stopping submodule services...${NC}"

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
    echo -e "${YELLOW}  settings.gradle.kts not found, scanning directories...${NC}"
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

echo -e "${GREEN}✓${NC} Detected modules: ${SUBMODULES[*]}"

STOPPED_SUBMODULES=()

for submodule in "${SUBMODULES[@]}"; do
    if [ -f "$submodule/stop-prod.sh" ]; then
        echo -e "${YELLOW}  Stopping $submodule/ services (production)${NC}"
        chmod +x "$submodule/stop-prod.sh"
        if [ "$REMOVE_VOLUMES" = true ]; then
            (cd "$submodule" && ./stop-prod.sh --volumes)
        else
            (cd "$submodule" && ./stop-prod.sh)
        fi
        STOPPED_SUBMODULES+=("$submodule")
        echo -e "${GREEN}✓${NC} $submodule services stopped"
    fi
done

if [ ${#STOPPED_SUBMODULES[@]} -gt 0 ]; then
    echo -e "${GREEN}✓${NC} Stopped ${#STOPPED_SUBMODULES[@]} submodule service(s): ${STOPPED_SUBMODULES[*]}"
fi

if [ "$REMOVE_VOLUMES" = true ]; then
    echo -e "${RED}⚠️  Data volumes have been removed${NC}"
fi

echo -e "${GREEN}✓${NC} All services stopped successfully"

