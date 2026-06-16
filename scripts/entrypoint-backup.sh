#!/bin/sh
set -e

mc alias set myminio http://minio:9000 "$MINIO_BACKUP_USER" "$MINIO_BACKUP_PASSWORD"

python3 /backup-exporter.py &

echo "$BACKUP_INTERVAL /backup.sh" > /tmp/crontab
exec supercronic /tmp/crontab
