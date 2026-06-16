--liquibase formatted sql

--changeset migration:V009_add_outbox_tables labels:9 splitStatements:false
CREATE TABLE IF NOT EXISTS order_outbox_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  aggregate_type VARCHAR(100) NOT NULL,
  aggregate_id VARCHAR(100) NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  payload JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  published_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS supply_outbox_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  aggregate_type VARCHAR(100) NOT NULL,
  aggregate_id VARCHAR(100) NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  payload JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  published_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_order_outbox_unpublished ON order_outbox_events (created_at) WHERE published_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_supply_outbox_unpublished ON supply_outbox_events (created_at) WHERE published_at IS NULL;

--rollback DROP INDEX IF EXISTS idx_supply_outbox_unpublished;
--rollback DROP INDEX IF EXISTS idx_order_outbox_unpublished;
--rollback DROP TABLE IF EXISTS supply_outbox_events;
--rollback DROP TABLE IF EXISTS order_outbox_events;
