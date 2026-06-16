#!/usr/bin/env sh
set -eu

MB_URL="${MB_URL:-http://metabase:3000}"
ADMIN_EMAIL="${MB_ADMIN_EMAIL:-admin@admin.com}"
ADMIN_PASSWORD="${MB_ADMIN_PASSWORD:-Admin1234!}"
CH_HOST="${CH_HOST:-clickhouse}"
CH_PORT="${CH_PORT:-8123}"
CH_DB="${CH_DB:-analytics}"
CH_USER="${CH_USER:-metabase}"
CH_PASSWORD="${CH_PASSWORD:-metabase_clickhouse_password}"

echo "Waiting for Metabase..."
until curl -sf "$MB_URL/api/health" | grep -q '"status":"ok"'; do
  sleep 5
done
echo "Metabase ready."

# Check if already set up
HAS_USER=$(curl -s "$MB_URL/api/session/properties" | grep -o '"has-user-setup":[^,}]*' | cut -d: -f2 | tr -d ' ')

if [ "$HAS_USER" = "true" ]; then
  echo "Already set up. Skipping init."
  exit 0
fi

SETUP_TOKEN=$(curl -s "$MB_URL/api/session/properties" | grep -o '"setup-token":"[^"]*"' | cut -d'"' -f4)
echo "Setup token: $SETUP_TOKEN"

SESSION=$(curl -s -X POST "$MB_URL/api/setup" \
  -H "Content-Type: application/json" \
  -d "{
    \"token\": \"$SETUP_TOKEN\",
    \"user\": {
      \"first_name\": \"Admin\",
      \"last_name\": \"User\",
      \"email\": \"$ADMIN_EMAIL\",
      \"password\": \"$ADMIN_PASSWORD\",
      \"site_name\": \"CraftBeer Analytics\"
    },
    \"prefs\": {
      \"site_name\": \"CraftBeer Analytics\",
      \"allow_tracking\": false
    }
  }" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "Setup session: $SESSION"

SESSION=$(curl -s -X POST "$MB_URL/api/session" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" \
  | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
echo "Session: $SESSION"

DB_ID=$(curl -s -X POST "$MB_URL/api/database" \
  -H "Content-Type: application/json" \
  -H "X-Metabase-Session: $SESSION" \
  -d "{
    \"engine\": \"clickhouse\",
    \"name\": \"ClickHouse Analytics\",
    \"details\": {
      \"host\": \"$CH_HOST\",
      \"port\": $CH_PORT,
      \"dbname\": \"$CH_DB\",
      \"user\": \"$CH_USER\",
      \"password\": \"$CH_PASSWORD\",
      \"ssl\": false,
      \"additional-options\": \"\"
    }
  }" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
echo "Database ID: $DB_ID"

echo "Waiting for metadata sync..."
until curl -s "$MB_URL/api/database/$DB_ID" \
  -H "X-Metabase-Session: $SESSION" \
  | grep -q '"initial_sync_status":"complete"'; do
  sleep 5
done
echo "Metadata synced."

Q1=$(curl -s -X POST "$MB_URL/api/card" \
  -H "Content-Type: application/json" \
  -H "X-Metabase-Session: $SESSION" \
  -d "{
    \"name\": \"Топ-10 самых заказываемых товаров\",
    \"display\": \"bar\",
    \"dataset_query\": {
      \"type\": \"native\",
      \"database\": $DB_ID,
      \"native\": {
        \"query\": \"SELECT b.name, sum(oi.quantity) AS total_qty FROM analytics.order_items oi JOIN analytics.product_variants pv ON oi.variant_id = pv.id JOIN analytics.beers b ON pv.beer_id = b.id GROUP BY b.name ORDER BY total_qty DESC LIMIT 10\"
      }
    },
    \"visualization_settings\": {
      \"graph.metrics\": [\"total_qty\"],
      \"graph.dimensions\": [\"name\"]
    }
  }" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
echo "Q1 (top ordered): $Q1"

Q2=$(curl -s -X POST "$MB_URL/api/card" \
  -H "Content-Type: application/json" \
  -H "X-Metabase-Session: $SESSION" \
  -d "{
    \"name\": \"Топ-10 самых прибыльных товаров\",
    \"display\": \"bar\",
    \"dataset_query\": {
      \"type\": \"native\",
      \"database\": $DB_ID,
      \"native\": {
        \"query\": \"SELECT b.name, sum(oi.quantity * oi.unit_price - oi.line_discount) AS revenue FROM analytics.order_items oi JOIN analytics.product_variants pv ON oi.variant_id = pv.id JOIN analytics.beers b ON pv.beer_id = b.id GROUP BY b.name ORDER BY revenue DESC LIMIT 10\"
      }
    },
    \"visualization_settings\": {
      \"graph.metrics\": [\"revenue\"],
      \"graph.dimensions\": [\"name\"]
    }
  }" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
echo "Q2 (top revenue): $Q2"

Q3=$(curl -s -X POST "$MB_URL/api/card" \
  -H "Content-Type: application/json" \
  -H "X-Metabase-Session: $SESSION" \
  -d "{
    \"name\": \"Динамика заказов по дням\",
    \"display\": \"combo\",
    \"dataset_query\": {
      \"type\": \"native\",
      \"database\": $DB_ID,
      \"native\": {
        \"query\": \"SELECT toDate(placed_at) AS day, count(*) AS orders, sum(total_amount) AS revenue FROM analytics.orders GROUP BY day ORDER BY day\"
      }
    },
    \"visualization_settings\": {
      \"graph.metrics\": [\"orders\", \"revenue\"],
      \"graph.dimensions\": [\"day\"],
      \"series_settings\": {\"orders\": {\"display\": \"line\", \"axis\": \"left\"}, \"revenue\": {\"display\": \"line\", \"axis\": \"right\"}}
    }
  }" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
echo "Q3 (timeline): $Q3"

DASH=$(curl -s -X POST "$MB_URL/api/dashboard" \
  -H "Content-Type: application/json" \
  -H "X-Metabase-Session: $SESSION" \
  -d '{
    "name": "CDC Analytics — Customer Orders",
    "description": "Real-time analytics via Debezium CDC pipeline"
  }' | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
echo "Dashboard: $DASH"

PUT_RESULT=$(curl -s -X PUT "$MB_URL/api/dashboard/$DASH/cards" \
  -H "Content-Type: application/json" \
  -H "X-Metabase-Session: $SESSION" \
  -d "{
    \"cards\": [
      {\"id\": -1, \"card_id\": $Q1, \"row\": 0, \"col\": 0,  \"size_x\": 12, \"size_y\": 8, \"parameter_mappings\": [], \"visualization_settings\": {}, \"series\": []},
      {\"id\": -2, \"card_id\": $Q2, \"row\": 0, \"col\": 12, \"size_x\": 12, \"size_y\": 8, \"parameter_mappings\": [], \"visualization_settings\": {}, \"series\": []},
      {\"id\": -3, \"card_id\": $Q3, \"row\": 8, \"col\": 0,  \"size_x\": 24, \"size_y\": 8, \"parameter_mappings\": [], \"visualization_settings\": {}, \"series\": []}
    ]
  }")
echo "Cards placed: $PUT_RESULT"

echo "Done. Dashboard $DASH at $MB_URL/dashboard/$DASH"
