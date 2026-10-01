#!/bin/sh
# Stop CareerConnect from the repo root: ./stop.sh
# Pass a port to stop a copy running elsewhere: ./stop.sh 8082
port="${1:-8081}"
if command -v lsof >/dev/null 2>&1; then
  pids=$(lsof -ti tcp:"$port" -sTCP:LISTEN)
  [ -n "$pids" ] && kill $pids
else
  # Git Bash on Windows has no lsof
  pids=$(netstat -ano | awk -v p=":$port" '$2 ~ p"$" && $4 == "LISTENING" {print $5}' | sort -u)
  for pid in $pids; do taskkill //PID "$pid" //F >/dev/null; done
fi
if [ -n "$pids" ]; then echo "Stopped the app on port $port."; else echo "Nothing is running on port $port."; fi
