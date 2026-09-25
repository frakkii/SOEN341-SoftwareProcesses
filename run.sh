#!/bin/sh
# Start CareerConnect from the repo root: ./run.sh
cd "$(dirname "$0")/src/backend" && exec ./mvnw spring-boot:run "$@"
