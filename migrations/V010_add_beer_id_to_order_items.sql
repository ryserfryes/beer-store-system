--liquibase formatted sql

--changeset migration:V010_add_beer_id_to_order_items labels:10 splitStatements:false
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS beer_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_order_items_beer_id ON order_items(beer_id);

--rollback DROP INDEX IF EXISTS idx_order_items_beer_id;
--rollback ALTER TABLE order_items DROP COLUMN IF EXISTS beer_id;
