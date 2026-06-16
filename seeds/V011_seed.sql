INSERT INTO review_eligible_customers (customer_id, order_id, created_at)
SELECT
    o.customer_id,
    o.id,
    COALESCE(o.picked_up_at, o.ready_for_pickup_at, o.placed_at) + make_interval(secs => 1)
FROM customer_orders o
WHERE o.status IN ('picked_up', 'ready_for_pickup')
ON CONFLICT (customer_id, order_id) DO NOTHING;
