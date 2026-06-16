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

label_filter=""
for i in $(seq 1 "${target_version}"); do
  [[ -z "${label_filter}" ]] && label_filter="${i}" || label_filter="${label_filter} or ${i}"
done

liquibase \
  --url="${TEST_DATABASE_JDBC_URL}" \
  --username="${POSTGRES_USER}" \
  --password="${POSTGRES_PASSWORD}" \
  --changelog-file=changelog-master.yaml \
  --search-path=/migrations \
  update \
  --label-filter="${label_filter}"