#!/usr/bin/env bash
set -euo pipefail

if [[ "${APP_ENV:-dev}" == "prod" ]]; then
  echo "APP_ENV=prod detected. Skipping synthetic seeding."
  exit 0
fi

echo "Waiting for database..."
until pg_isready -d "${DATABASE_URL}" >/dev/null 2>&1; do
  sleep 1
done
echo "Database is ready."

resolve_target_version() {
  if [[ -n "${MIGRATION_VERSION:-}" ]]; then
    echo "${MIGRATION_VERSION}"
    return
  fi

  psql "${DATABASE_URL}" -At -v ON_ERROR_STOP=1 -c \
    "SELECT COALESCE(MAX(CAST(labels AS INTEGER)), 0) FROM databasechangelog WHERE labels ~ '^[0-9]+$';"
}

target_version_raw="$(resolve_target_version)"
if [[ "${target_version_raw}" =~ ^0*([0-9]+)$ ]]; then
  target_version="${BASH_REMATCH[1]}"
else
  echo "Unsupported MIGRATION_VERSION format: ${target_version_raw}. Expected integer version." >&2
  exit 1
fi

applied_count=0
for file in $(find /seeds -maxdepth 1 -type f -name 'V*_seed.sql' | sort -V); do
  [[ -e "${file}" ]] || continue
  filename="$(basename "${file}")"
  if [[ ! "${filename}" =~ ^V([0-9]+)_seed\.sql$ ]]; then
    continue
  fi
  version="${BASH_REMATCH[1]}"

  if (( 10#"${version}" <= 10#"${target_version}" )); then
    echo "Applying seed file ${file} (SEED_COUNT=${SEED_COUNT})..."
    psql "${DATABASE_URL}" -v ON_ERROR_STOP=1 -v seed_count="${SEED_COUNT}" -f "${file}"
    applied_count=$((applied_count + 1))
  fi
done

if (( applied_count == 0 )); then
  echo "No compatible seed file found for migration version ${target_version_raw}" >&2
  exit 1
fi

echo "Seeding completed. Applied ${applied_count} seed file(s)."
