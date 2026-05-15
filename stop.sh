#!/bin/bash

echo "Stopping Redis..."
docker-compose -f infra/docker-compose.yml down --remove-orphans -v
echo "Environment shutdown complete."

