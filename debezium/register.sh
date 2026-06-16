#!/usr/bin/env sh
set -eu

CONNECT_URL="${CONNECT_URL:-http://kafka-connect:8083}"
CONNECTOR_NAME="postgres-connector"

echo "Waiting for Kafka Connect..."
until curl -sf "$CONNECT_URL/connectors" > /dev/null 2>&1; do
  sleep 3
done
echo "Kafka Connect ready."

echo "Registering connector $CONNECTOR_NAME (with retry)..."
until curl -sf -X PUT \
  -H "Content-Type: application/json" \
  --data "@/debezium/connector.config.json" \
  "$CONNECT_URL/connectors/$CONNECTOR_NAME/config" > /dev/null 2>&1; do
  echo "Registration failed (DB not ready yet?), retrying in 5s..."
  sleep 5
done

echo "Connector registered. Status:"
curl -sf "$CONNECT_URL/connectors/$CONNECTOR_NAME/status"
echo ""
