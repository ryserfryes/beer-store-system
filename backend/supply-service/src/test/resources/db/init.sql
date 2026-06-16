CREATE TYPE purchase_order_status AS ENUM (
    'waiting_approval',
    'drafting',
    'partially_delivered',
    'delivered',
    'received',
    'canceled'
);
