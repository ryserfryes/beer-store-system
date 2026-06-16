--liquibase formatted sql
--changeset migration:V011_add_completed_orders_for_reviews labels:11 splitStatements:false
CREATE TABLE IF NOT EXISTS review_eligible_customers (
  customer_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY (customer_id, order_id)
);
--rollback DROP TABLE IF EXISTS review_eligible_customers;
