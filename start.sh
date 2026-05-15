#!/bin/bash

set -e

echo "[1/4] Running tests and coverage..."
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification

echo "[2/4] Starting Redis..."
docker-compose -f infra/docker-compose.yml up -d

echo "[3/4] Starting FinTechTransactionService..."
./gradlew bootRun

