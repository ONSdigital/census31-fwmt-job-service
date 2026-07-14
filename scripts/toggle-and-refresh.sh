#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  ./scripts/toggle-and-refresh.sh --property <name> --value <value> [options]

Examples:
  ./scripts/toggle-and-refresh.sh --property feature-flags.hh.create --value true
  ./scripts/toggle-and-refresh.sh --url http://localhost:8025 --property feature-flags.ce.create --value false

Options:
  --url <base-url>           Service base URL (default: http://localhost:8025)
  --username <username>      Basic auth username (default: user)
  --password <password>      Basic auth password (default: password)
  --property <name>          Property name to set via env endpoint (required)
  --value <value>            Property value to set (required)
  --health-timeout <seconds> Health wait timeout in seconds (default: 20)
EOF
}

BASE_URL="http://localhost:8025"
USERNAME="user"
PASSWORD="password"
PROPERTY_NAME=""
PROPERTY_VALUE=""
HEALTH_TIMEOUT=20

while [[ $# -gt 0 ]]; do
  case "$1" in
    --url)
      BASE_URL="$2"
      shift 2
      ;;
    --username)
      USERNAME="$2"
      shift 2
      ;;
    --password)
      PASSWORD="$2"
      shift 2
      ;;
    --property)
      PROPERTY_NAME="$2"
      shift 2
      ;;
    --value)
      PROPERTY_VALUE="$2"
      shift 2
      ;;
    --health-timeout)
      HEALTH_TIMEOUT="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ -z "$PROPERTY_NAME" || -z "$PROPERTY_VALUE" ]]; then
  echo "Both --property and --value are required" >&2
  usage
  exit 1
fi

post_json() {
  local path="$1"
  local payload="$2"
  local status

  status="$(curl -sS -o /tmp/fwmt-toggle-response.$$ -w "%{http_code}" \
    -u "$USERNAME:$PASSWORD" \
    -H "Content-Type: application/json" \
    -X POST \
    --data "$payload" \
    "$BASE_URL$path" || true)"

  if [[ "$status" == "404" ]]; then
    return 2
  fi

  if [[ "$status" != 2* ]]; then
    echo "Request to $path failed with HTTP $status" >&2
    cat /tmp/fwmt-toggle-response.$$ >&2 || true
    rm -f /tmp/fwmt-toggle-response.$$ || true
    return 1
  fi

  rm -f /tmp/fwmt-toggle-response.$$ || true
  return 0
}

set_payload="{\"name\":\"$PROPERTY_NAME\",\"value\":\"$PROPERTY_VALUE\"}"
if ! post_json "/actuator/env" "$set_payload"; then
  if [[ $? -ne 2 ]] || ! post_json "/env" "$set_payload"; then
    echo "Could not set property. Ensure env endpoint is exposed and APP_TESTING=true." >&2
    exit 1
  fi
fi

echo "Updated $PROPERTY_NAME=$PROPERTY_VALUE"

if ! post_json "/actuator/refresh" "{}"; then
  if [[ $? -ne 2 ]] || ! post_json "/refresh" "{}"; then
    echo "Could not refresh. Ensure refresh endpoint is exposed and APP_TESTING=true." >&2
    exit 1
  fi
fi

echo "Refresh triggered"

deadline=$(( $(date +%s) + HEALTH_TIMEOUT ))
while [[ $(date +%s) -lt "$deadline" ]]; do
  health_status="$(curl -sS -u "$USERNAME:$PASSWORD" "$BASE_URL/health" | grep -o '"status":"[A-Z]*"' | head -n 1 | cut -d '"' -f 4 || true)"
  if [[ "$health_status" == "UP" ]]; then
    echo "Service health is UP"
    exit 0
  fi
  sleep 1
done

echo "Timed out waiting for health status UP at $BASE_URL/health" >&2
exit 1
