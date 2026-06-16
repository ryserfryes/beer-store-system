WITH src AS (
  SELECT gs AS n
  FROM generate_series(1, GREATEST(1, :seed_count::INT * 5)) AS gs
)
INSERT INTO customers (email, phone, first_name, last_name, birth_date)
SELECT
  format('customer%s@example.com', lpad(src.n::TEXT, 4, '0')),
  format('+7999000%s', lpad(src.n::TEXT, 4, '0')),
  format('Имя%s', src.n),
  format('Фамилия%s', src.n),
  DATE '1970-01-01' + (((src.n * 127 + 3000) % 18000) || ' days')::INTERVAL
FROM src
ON CONFLICT (email) DO NOTHING;

INSERT INTO carts (customer_id)
SELECT c.id
FROM customers c
ON CONFLICT (customer_id) DO NOTHING;

WITH ranked_carts AS (
  SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
  FROM carts
),
ranked_variants AS (
  SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
  FROM product_variants
)
INSERT INTO cart_items (cart_id, variant_id, quantity)
SELECT
  rc.id,
  rv.id,
  1 + (rc.rn % 5)
FROM ranked_carts rc
JOIN LATERAL (
  SELECT id, rn
  FROM ranked_variants
  ORDER BY rn
  OFFSET ((rc.rn - 1) % (SELECT COUNT(*) FROM ranked_variants))
  LIMIT 1
) AS rv ON TRUE
ON CONFLICT (cart_id, variant_id) DO UPDATE
SET quantity = EXCLUDED.quantity;

-- Orders scattered over 2 years, pseudo-random amounts
WITH src AS (
  SELECT gs AS n
  FROM generate_series(1, GREATEST(1, :seed_count::INT * 2)) AS gs
)
INSERT INTO customer_orders (
  customer_id,
  pickup_point_id,
  status,
  subtotal_amount,
  discount_amount,
  total_amount,
  pickup_code,
  placed_at
)
SELECT
  c.id,
  pp.id,
  'paid'::order_status,
  500 + (src.n * 37 % 100) * 15,
  CASE WHEN src.n % 4 = 0 THEN (src.n % 5 + 1) * 50 ELSE 0 END,
  500 + (src.n * 37 % 100) * 15 - CASE WHEN src.n % 4 = 0 THEN (src.n % 5 + 1) * 50 ELSE 0 END,
  format('Код-выдачи-%s', lpad(src.n::TEXT, 6, '0')),
  NOW() - make_interval(
    days  => (src.n * 13) % 730,
    hours => (src.n *  7) % 24,
    mins  => (src.n *  3) % 60
  )
FROM src
JOIN LATERAL (
  SELECT id
  FROM customers
  ORDER BY id
  OFFSET ((src.n - 1) % (SELECT COUNT(*) FROM customers))
  LIMIT 1
) AS c ON TRUE
JOIN LATERAL (
  SELECT id
  FROM pickup_points
  ORDER BY id
  OFFSET ((src.n - 1) % (SELECT COUNT(*) FROM pickup_points))
  LIMIT 1
) AS pp ON TRUE
ON CONFLICT (pickup_code) DO NOTHING;

WITH ranked_orders AS (
  SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
  FROM customer_orders
),
ranked_variants AS (
  SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
  FROM product_variants
),
ranked_batches AS (
  SELECT id, variant_id, ROW_NUMBER() OVER (ORDER BY id) AS rn
  FROM inventory_batches
)
-- Pareto-skewed variant assignment: ~60% of orders go to first 20 variants (top 10 beers),
-- remaining 40% spread across all variants — creates visible top-10 in both quantity and revenue metrics
INSERT INTO order_items (order_id, variant_id, batch_id, quantity, unit_price, line_discount)
SELECT
  ro.id,
  rv.id,
  rb.id,
  -- top variants also get higher quantities
  CASE WHEN rv.rn <= 20 THEN 3 + (ro.rn % 5) ELSE 1 + (ro.rn % 3) END,
  ROUND((80 + (rv.rn * 7 % 60) + (ro.rn % 20) * 2.5)::NUMERIC, 2),
  CASE WHEN ro.rn % 7 = 0 THEN ROUND((20 + ro.rn % 40)::NUMERIC, 2) ELSE 0 END
FROM ranked_orders ro
JOIN LATERAL (
  SELECT id, rn
  FROM ranked_variants
  ORDER BY rn
  OFFSET (
    CASE
      WHEN ro.rn % 10 < 6 THEN ro.rn % 20          -- 60% → first 20 variants (top 10 beers)
      ELSE (ro.rn * 13) % (SELECT COUNT(*) FROM ranked_variants)  -- 40% → full spread
    END
  )
  LIMIT 1
) AS rv ON TRUE
LEFT JOIN LATERAL (
  SELECT id
  FROM ranked_batches
  WHERE variant_id = rv.id
  ORDER BY rn
  LIMIT 1
) AS rb ON TRUE
ON CONFLICT (order_id, variant_id) DO NOTHING;
