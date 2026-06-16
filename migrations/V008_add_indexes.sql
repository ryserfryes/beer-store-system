--liquibase formatted sql
--changeset migration:V008_add_indexes labels:8 splitStatements:false
CREATE INDEX idx_customer_orders_status     ON customer_orders(status);
CREATE INDEX idx_customer_orders_placed_at  ON customer_orders(placed_at);
CREATE INDEX idx_customer_orders_customer_id ON customer_orders(customer_id);
CREATE INDEX idx_order_items_order_id       ON order_items(order_id);
CREATE INDEX idx_order_items_variant_id     ON order_items(variant_id);
CREATE INDEX idx_product_variants_beer_id   ON product_variants(beer_id);
CREATE INDEX idx_product_views_variant_id   ON product_views(variant_id);
CREATE INDEX idx_order_audit_log_order_id   ON order_audit_log(order_id);
--rollback DROP INDEX IF EXISTS idx_customer_orders_status;
--rollback DROP INDEX IF EXISTS idx_customer_orders_placed_at;
--rollback DROP INDEX IF EXISTS idx_customer_orders_customer_id;
--rollback DROP INDEX IF EXISTS idx_order_items_order_id;
--rollback DROP INDEX IF EXISTS idx_order_items_variant_id;
--rollback DROP INDEX IF EXISTS idx_product_variants_beer_id;
--rollback DROP INDEX IF EXISTS idx_product_views_variant_id;
--rollback DROP INDEX IF EXISTS idx_order_audit_log_order_id;
