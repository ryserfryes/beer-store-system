INSERT INTO employees (email, first_name, last_name, role, hired_at, is_active)
VALUES
  ('manager@example.com', 'Дементий', 'Менеджер', 'manager', CURRENT_DATE - 400, TRUE),
  ('picker@example.com', 'Алексей', 'Сборщик', 'picker', CURRENT_DATE - 250, TRUE),
  ('accountant@example.com', 'Бунбун', 'Бухгалтер', 'accountant', CURRENT_DATE - 300, TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO order_status_history (order_id, changed_by_employee_id, from_status, to_status, changed_at)
SELECT
  o.id,
  e.id,
  'pending'::order_status,
  'paid'::order_status,
  o.placed_at + INTERVAL '2 minutes'
FROM customer_orders o
JOIN LATERAL (
  SELECT id
  FROM employees
  WHERE role = 'manager'
  ORDER BY id
  LIMIT 1
) AS e ON TRUE
WHERE NOT EXISTS (
  SELECT 1
  FROM order_status_history h
  WHERE h.order_id = o.id
    AND h.to_status = 'paid'
);

WITH ranked_customers AS (
  SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
  FROM customers
),
ranked_beers AS (
  SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
  FROM beers
)
INSERT INTO reviews (customer_id, beer_id, rating, comment)
SELECT
  rc.id,
  rb.id,
  3 + (rc.rn % 3),
  format('Тестовый обзор %s', rc.rn)
FROM ranked_customers rc
JOIN LATERAL (
  SELECT id, rn
  FROM ranked_beers
  ORDER BY rn
  OFFSET ((rc.rn - 1) % (SELECT COUNT(*) FROM ranked_beers))
  LIMIT 1
) AS rb ON TRUE
ON CONFLICT (customer_id, beer_id) DO NOTHING;

UPDATE customer_orders
SET
  status = 'ready_for_pickup'::order_status,
  ready_for_pickup_at = COALESCE(ready_for_pickup_at, placed_at + INTERVAL '2 hours'),
  pickup_expires_at = COALESCE(pickup_expires_at, placed_at + INTERVAL '3 days')
WHERE status = 'paid'
  AND id IN (
    SELECT id
    FROM customer_orders
    ORDER BY id
    LIMIT GREATEST(1, :seed_count::INT)
  );

INSERT INTO order_status_history (order_id, changed_by_employee_id, from_status, to_status, changed_at)
SELECT
  o.id,
  e.id,
  'paid'::order_status,
  'ready_for_pickup'::order_status,
  o.ready_for_pickup_at
FROM customer_orders o
JOIN LATERAL (
  SELECT id
  FROM employees
  WHERE role = 'picker'
  ORDER BY id
  LIMIT 1
) AS e ON TRUE
WHERE o.status = 'ready_for_pickup'
  AND NOT EXISTS (
    SELECT 1
    FROM order_status_history h
    WHERE h.order_id = o.id
      AND h.to_status = 'ready_for_pickup'
  );
