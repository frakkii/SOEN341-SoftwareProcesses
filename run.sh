#!/bin/sh
# Start CareerConnect from the repo root: ./run.sh
# Checks the two most common reasons the app fails to start, so the error is
# clear instead of Maven's generic "Process terminated with exit code: 1".

# 1. No database password (not needed when SUPABASE_DB_URL points somewhere else)
if [ -z "$SUPABASE_DB_URL" ] && [ -z "$SUPABASE_DB_PASSWORD" ]; then
    echo
    echo "SUPABASE_DB_PASSWORD is not set in this terminal, so the app can't connect to the database."
    echo "Set it first (README, step 2), then run again:"
    echo "  export SUPABASE_DB_PASSWORD='soen341proj'"
    echo
    exit 1
fi

# 2. Another copy of the app is already using port 8081
if command -v lsof >/dev/null 2>&1; then
    lsof -iTCP:8081 -sTCP:LISTEN >/dev/null 2>&1 && in_use=1
elif command -v netstat >/dev/null 2>&1; then
    netstat -an 2>/dev/null | grep -E '[:.]8081 .*LISTEN' >/dev/null && in_use=1
fi
if [ -n "$in_use" ]; then
    echo
    echo "Port 8081 is already in use, probably by another copy of the app (another terminal or your IDE)."
    echo "Run ./stop.sh to close it, then run again."
    echo
    exit 1
fi

cd "$(dirname "$0")/src/backend" && exec ./mvnw spring-boot:run "$@"
