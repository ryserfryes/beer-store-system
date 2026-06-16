#!/usr/bin/env bash
set -euo pipefail

export PGPASSWORD="${POSTGRES_PASSWORD}"

JDBC_URL="jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB}?sslmode=disable"
TEST_JDBC_URL="jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${TEST_POSTGRES_DB}?sslmode=disable"
TEST_DATABASE_URL="postgresql://${POSTGRES_USER}:${POSTGRES_PASSWORD}@${POSTGRES_HOST}:${POSTGRES_PORT}/${TEST_POSTGRES_DB}?sslmode=disable"

HAPROXY_URL="postgresql://${POSTGRES_USER}:${POSTGRES_PASSWORD}@${POSTGRES_HOST}:${POSTGRES_PORT}/postgres?sslmode=disable"

echo "Waiting for PostgreSQL primary via HAProxy..."
until psql "${HAPROXY_URL}" -At -c "SELECT NOT pg_is_in_recovery();" 2>/dev/null | grep -q "t"; do
  sleep 2
done
echo "PostgreSQL primary is ready."

export TEST_DATABASE_URL
export TEST_DATABASE_JDBC_URL="${TEST_JDBC_URL}"

if [[ "${RUN_MIGRATION_TESTS:-true}" == "true" ]]; then
  echo "Recreating test database ${TEST_POSTGRES_DB} for Seqwall..."
  until psql "${HAPROXY_URL}" -v ON_ERROR_STOP=1 \
      -c "DROP DATABASE IF EXISTS \"${TEST_POSTGRES_DB}\";" \
      -c "CREATE DATABASE \"${TEST_POSTGRES_DB}\";" 2>/dev/null; do
    echo "HAProxy not yet converged, retrying..."
    sleep 2
  done
  echo "Running Seqwall staircase tests..."
  seqwall staircase \
    --postgres-url "${TEST_DATABASE_URL}" \
    --migrations-path "/migrations" \
    --upgrade "/scripts/migrate-up-one.sh {current_migration}" \
    --downgrade "/scripts/migrate-down-one.sh {current_migration}" \
    --migrations-extension ".sql" \
    --schema public
  echo "Seqwall passed."
else
  echo "Seqwall skipped (RUN_MIGRATION_TESTS=false)."
fi

LIQUIBASE_ARGS=(
  --url="${JDBC_URL}"
  --username="${POSTGRES_USER}"
  --password="${POSTGRES_PASSWORD}"
  --changelog-file=changelog-master.yaml
  --search-path=/migrations
)

if [[ -n "${MIGRATION_VERSION:-}" ]]; then
  label_filter=""
  for i in $(seq 1 "${MIGRATION_VERSION}"); do
    [[ -z "${label_filter}" ]] && label_filter="${i}" || label_filter="${label_filter},${i}"
  done
  LIQUIBASE_ARGS+=(--label-filter="${label_filter}")
fi

LIQUIBASE_ARGS+=(update)

echo "Applying migrations to main database..."
liquibase "${LIQUIBASE_ARGS[@]}"

echo "Migrations applied successfully."