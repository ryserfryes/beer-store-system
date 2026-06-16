#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <current_migration_path>" >&2
  exit 1
fi

if [[ -z "${TEST_DATABASE_JDBC_URL:-}" ]]; then
  echo "TEST_DATABASE_JDBC_URL is not set" >&2
  exit 1
fi

if [[ -z "${TEST_DATABASE_URL:-}" ]]; then
  echo "TEST_DATABASE_URL is not set" >&2
  exit 1
fi

if [[ -z "${POSTGRES_USER:-}" || -z "${POSTGRES_PASSWORD:-}" ]]; then
  echo "POSTGRES_USER/POSTGRES_PASSWORD are required" >&2
  exit 1
fi

migration_path="$1"
up_file="$(basename "${migration_path}")"

if [[ ! "${up_file}" =~ ^V([0-9]+)_.+\.sql$ ]]; then
  echo "Unexpected migration file format: ${up_file}" >&2
  exit 1
fi

target_version=$(( 10#${BASH_REMATCH[1]} ))

current_max=$(psql "${TEST_DATABASE_URL}" -At -c \
  "SELECT COALESCE(MAX(labels::INTEGER), 0) FROM databasechangelog WHERE labels ~ '^[0-9]+\$';" \
  2>/dev/null || echo "0")

current_max="${current_max:-0}"

if [[ "${current_max}" -lt "${target_version}" ]]; then
  echo "Nothing to rollback: current_max=${current_max} < target=${target_version}" >&2
  exit 1
fi

rollback_count=$(( current_max - target_version + 1 ))

echo "Rolling back ${rollback_count} changeset(s) (current_max=${current_max}, target=${target_version})"

liquibase \
  --url="${TEST_DATABASE_JDBC_URL}" \
  --username="${POSTGRES_USER}" \
  --password="${POSTGRES_PASSWORD}" \
  --changelog-file=changelog-master.yaml \
  --search-path=/migrations \
  rollback-count "${rollback_count}"
