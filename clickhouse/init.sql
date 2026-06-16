CREATE DATABASE IF NOT EXISTS analytics;

CREATE TABLE IF NOT EXISTS analytics.cdc_orders_raw (raw String)
ENGINE = Kafka
SETTINGS
    kafka_broker_list    = 'kafka:9092',
    kafka_topic_list     = 'cdc.public.customer_orders',
    kafka_group_name     = 'clickhouse-cdc-craft-beer',
    kafka_format         = 'JSONAsString',
    kafka_max_block_size = 65536;

CREATE TABLE IF NOT EXISTS analytics.orders
(
    id                   Int64,
    customer_id          Int64,
    pickup_point_id      Int64,
    status               LowCardinality(String),
    subtotal_amount      Decimal(12, 2),
    discount_amount      Decimal(12, 2),
    total_amount         Decimal(12, 2),
    pickup_code          String,
    placed_at            DateTime64(6, 'UTC'),
    ready_for_pickup_at  Nullable(DateTime64(6, 'UTC')),
    pickup_expires_at    Nullable(DateTime64(6, 'UTC')),
    picked_up_at         Nullable(DateTime64(6, 'UTC')),
    cdc_op               LowCardinality(String),
    cdc_ts_ms            Int64
)
ENGINE = ReplacingMergeTree(cdc_ts_ms)
PARTITION BY toYYYYMM(placed_at)
ORDER BY id;

CREATE MATERIALIZED VIEW IF NOT EXISTS analytics.cdc_orders_mv TO analytics.orders AS
SELECT
    JSONExtractInt(raw, 'after', 'id')                                          AS id,
    JSONExtractInt(raw, 'after', 'customer_id')                                 AS customer_id,
    JSONExtractInt(raw, 'after', 'pickup_point_id')                             AS pickup_point_id,
    JSONExtractString(raw, 'after', 'status')                                   AS status,
    toDecimal64(JSONExtractString(raw, 'after', 'subtotal_amount'), 2)          AS subtotal_amount,
    toDecimal64(JSONExtractString(raw, 'after', 'discount_amount'), 2)          AS discount_amount,
    toDecimal64(JSONExtractString(raw, 'after', 'total_amount'), 2)             AS total_amount,
    JSONExtractString(raw, 'after', 'pickup_code')                              AS pickup_code,
    parseDateTime64BestEffort(JSONExtractString(raw, 'after', 'placed_at'), 6) AS placed_at,
    if(JSONExtractString(raw, 'after', 'ready_for_pickup_at') != '',
        parseDateTime64BestEffort(JSONExtractString(raw, 'after', 'ready_for_pickup_at'), 6), NULL) AS ready_for_pickup_at,
    if(JSONExtractString(raw, 'after', 'pickup_expires_at') != '',
        parseDateTime64BestEffort(JSONExtractString(raw, 'after', 'pickup_expires_at'), 6), NULL)   AS pickup_expires_at,
    if(JSONExtractString(raw, 'after', 'picked_up_at') != '',
        parseDateTime64BestEffort(JSONExtractString(raw, 'after', 'picked_up_at'), 6), NULL)        AS picked_up_at,
    JSONExtractString(raw, 'op')                                                AS cdc_op,
    JSONExtractInt(raw, 'ts_ms')                                                AS cdc_ts_ms
FROM analytics.cdc_orders_raw
WHERE JSONExtractString(raw, 'op') IN ('c', 'u', 'r')
  AND JSONExtractString(raw, 'after', 'placed_at') != '';

CREATE TABLE IF NOT EXISTS analytics.cdc_order_items_raw (raw String)
ENGINE = Kafka
SETTINGS
    kafka_broker_list    = 'kafka:9092',
    kafka_topic_list     = 'cdc.public.order_items',
    kafka_group_name     = 'clickhouse-cdc-craft-beer',
    kafka_format         = 'JSONAsString',
    kafka_max_block_size = 65536;

CREATE TABLE IF NOT EXISTS analytics.order_items
(
    id            Int64,
    order_id      Int64,
    variant_id    Int64,
    beer_id       Int64,
    quantity      Int32,
    unit_price    Decimal(12, 2),
    line_discount Decimal(12, 2),
    cdc_op        LowCardinality(String),
    cdc_ts_ms     Int64
)
ENGINE = ReplacingMergeTree(cdc_ts_ms)
ORDER BY id;

CREATE MATERIALIZED VIEW IF NOT EXISTS analytics.cdc_order_items_mv TO analytics.order_items AS
SELECT
    JSONExtractInt(raw, 'after', 'id')                                 AS id,
    JSONExtractInt(raw, 'after', 'order_id')                           AS order_id,
    JSONExtractInt(raw, 'after', 'variant_id')                         AS variant_id,
    JSONExtractInt(raw, 'after', 'beer_id')                            AS beer_id,
    JSONExtractInt(raw, 'after', 'quantity')                           AS quantity,
    toDecimal64(JSONExtractString(raw, 'after', 'unit_price'), 2)      AS unit_price,
    toDecimal64(JSONExtractString(raw, 'after', 'line_discount'), 2)   AS line_discount,
    JSONExtractString(raw, 'op')                                        AS cdc_op,
    JSONExtractInt(raw, 'ts_ms')                                        AS cdc_ts_ms
FROM analytics.cdc_order_items_raw
WHERE JSONExtractString(raw, 'op') IN ('c', 'u', 'r');

CREATE TABLE IF NOT EXISTS analytics.cdc_beers_raw (raw String)
ENGINE = Kafka
SETTINGS
    kafka_broker_list    = 'kafka:9092',
    kafka_topic_list     = 'cdc.public.beers',
    kafka_group_name     = 'clickhouse-cdc-craft-beer',
    kafka_format         = 'JSONAsString',
    kafka_max_block_size = 65536;

CREATE TABLE IF NOT EXISTS analytics.beers
(
    id         Int64,
    name       String,
    abv        Decimal(4, 2),
    is_active  UInt8,
    cdc_op     LowCardinality(String),
    cdc_ts_ms  Int64
)
ENGINE = ReplacingMergeTree(cdc_ts_ms)
ORDER BY id;

CREATE MATERIALIZED VIEW IF NOT EXISTS analytics.cdc_beers_mv TO analytics.beers AS
SELECT
    JSONExtractInt(raw, 'after', 'id')            AS id,
    JSONExtractString(raw, 'after', 'name')        AS name,
    toDecimal64(JSONExtractString(raw, 'after', 'abv'), 2) AS abv,
    JSONExtractUInt(raw, 'after', 'is_active')     AS is_active,
    JSONExtractString(raw, 'op')                   AS cdc_op,
    JSONExtractInt(raw, 'ts_ms')                   AS cdc_ts_ms
FROM analytics.cdc_beers_raw
WHERE JSONExtractString(raw, 'op') IN ('c', 'u', 'r');

CREATE TABLE IF NOT EXISTS analytics.cdc_product_variants_raw (raw String)
ENGINE = Kafka
SETTINGS
    kafka_broker_list    = 'kafka:9092',
    kafka_topic_list     = 'cdc.public.product_variants',
    kafka_group_name     = 'clickhouse-cdc-craft-beer',
    kafka_format         = 'JSONAsString',
    kafka_max_block_size = 65536;

CREATE TABLE IF NOT EXISTS analytics.product_variants
(
    id        Int64,
    beer_id   Int64,
    sku       String,
    cdc_op    LowCardinality(String),
    cdc_ts_ms Int64
)
ENGINE = ReplacingMergeTree(cdc_ts_ms)
ORDER BY id;

CREATE MATERIALIZED VIEW IF NOT EXISTS analytics.cdc_product_variants_mv TO analytics.product_variants AS
SELECT
    JSONExtractInt(raw, 'after', 'id')       AS id,
    JSONExtractInt(raw, 'after', 'beer_id')  AS beer_id,
    JSONExtractString(raw, 'after', 'sku')   AS sku,
    JSONExtractString(raw, 'op')             AS cdc_op,
    JSONExtractInt(raw, 'ts_ms')             AS cdc_ts_ms
FROM analytics.cdc_product_variants_raw
WHERE JSONExtractString(raw, 'op') IN ('c', 'u', 'r');

CREATE USER IF NOT EXISTS metabase IDENTIFIED BY 'metabase_clickhouse_password';
GRANT SELECT ON analytics.* TO metabase;
