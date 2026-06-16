#!/bin/sh
set -e

FILENAME="backup_$(date +%Y%m%d_%H%M%S).sql.gz"

echo "Starting backup: $FILENAME"
PGPASSWORD="$POSTGRES_PASSWORD" pg_dump -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" "$POSTGRES_DB" \
  | gzip | mc pipe "myminio/$BUCKET_BACKUP_NAME/$FILENAME"

mc ls "myminio/$BUCKET_BACKUP_NAME" --json \
  | awk -F'"key":"' '{print $2}' \
  | awk -F'"' '{print $1}' \
  | sort \
  | head -n "-$BACKUP_RETENTION_COUNT" \
  | while read -r f; do
      echo "Deleting old backup: $f"
      mc rm "myminio/$BUCKET_BACKUP_NAME/$f"
    done

echo "Backup done: $FILENAME"
SIZE=$(mc ls --json "myminio/$BUCKET_BACKUP_NAME/$FILENAME" | awk -F'"size":' '{print $2}' | awk -F',' '{print $1}')
TIMESTAMP=$(date +%s)

echo "$TIMESTAMP" > /tmp/backup_state
echo "$SIZE" >> /tmp/backup_state
