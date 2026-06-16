#!/bin/sh

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER "${PG_EXPORTER_USER}" WITH PASSWORD '${PG_EXPORTER_PASSWORD}';
    ALTER USER "${PG_EXPORTER_USER}" SET SEARCH_PATH TO postgres_exporter,pg_catalog;
    GRANT CONNECT ON DATABASE "${POSTGRES_DB}" TO "${PG_EXPORTER_USER}";
    GRANT pg_monitor TO "${PG_EXPORTER_USER}";
EOSQL

echo "Exporter user created"