#!/usr/bin/env bash
set -euo pipefail

PRIMARY=""
for node in pg-node1 pg-node2 pg-node3; do
  role=$(docker exec "$node" curl -sf http://localhost:8008/patroni 2>/dev/null \
    | python3 -c "import sys,json; print(json.load(sys.stdin).get('role',''))" 2>/dev/null || echo "")
  if [ "$role" = "primary" ] || [ "$role" = "master" ]; then
    PRIMARY="$node"; break
  fi
done

if [ -z "$PRIMARY" ]; then
  echo "ERROR: cannot find primary node" >&2; exit 1
fi
echo "Primary: $PRIMARY"

docker exec -i "$PRIMARY" psql -U postgres -d craft_beer_store <<'SQL'
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'debezium_user') THEN
    CREATE USER debezium_user WITH REPLICATION LOGIN PASSWORD 'debezium_password';
  END IF;
END
$$;

GRANT SELECT ON ALL TABLES IN SCHEMA public TO debezium_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO debezium_user;

-- Allow debezium_user to use logical replication slots
GRANT pg_read_all_data TO debezium_user;

SELECT rolname, rolreplication FROM pg_roles WHERE rolname = 'debezium_user';
SQL

echo "Debezium user ready."
