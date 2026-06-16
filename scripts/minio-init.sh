#!/bin/sh
until mc alias set myminio http://minio:9000 "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD"; do
  sleep 2
done

mc mb --ignore-existing "myminio/$BUCKET_BACKUP_NAME"

mc admin user add myminio "$MINIO_BACKUP_USER" "$MINIO_BACKUP_PASSWORD"

cat > /tmp/policy.json << EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::${BUCKET_BACKUP_NAME}",
        "arn:aws:s3:::${BUCKET_BACKUP_NAME}/*"
      ]
    }
  ]
}
EOF

mc admin policy create myminio backup-policy /tmp/policy.json
mc admin policy attach myminio backup-policy --user "$MINIO_BACKUP_USER"

echo "MinIO init done"
