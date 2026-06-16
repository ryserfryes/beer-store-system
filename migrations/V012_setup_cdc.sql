--liquibase formatted sql

--changeset migration:V012_setup_cdc labels:12 splitStatements:false
GRANT SELECT ON ALL TABLES IN SCHEMA public TO debezium_user;
ALTER TABLE customer_orders REPLICA IDENTITY FULL;
ALTER TABLE order_items REPLICA IDENTITY FULL;
ALTER TABLE beers REPLICA IDENTITY FULL;
ALTER TABLE product_variants REPLICA IDENTITY FULL;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_publication WHERE pubname = 'debezium_pub') THEN
    EXECUTE 'CREATE PUBLICATION debezium_pub FOR TABLE customer_orders, order_items, beers, product_variants';
  ELSE
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'debezium_pub' AND tablename = 'order_items') THEN
      EXECUTE 'ALTER PUBLICATION debezium_pub ADD TABLE order_items';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'debezium_pub' AND tablename = 'beers') THEN
      EXECUTE 'ALTER PUBLICATION debezium_pub ADD TABLE beers';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'debezium_pub' AND tablename = 'product_variants') THEN
      EXECUTE 'ALTER PUBLICATION debezium_pub ADD TABLE product_variants';
    END IF;
  END IF;
END
$$;

--rollback REVOKE SELECT ON ALL TABLES IN SCHEMA public FROM debezium_user;
--rollback ALTER TABLE customer_orders REPLICA IDENTITY DEFAULT;
--rollback ALTER TABLE order_items REPLICA IDENTITY DEFAULT;
--rollback ALTER TABLE beers REPLICA IDENTITY DEFAULT;
--rollback ALTER TABLE product_variants REPLICA IDENTITY DEFAULT;
--rollback DROP PUBLICATION IF EXISTS debezium_pub;
