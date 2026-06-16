WITH src AS (
  SELECT gs AS n
  FROM generate_series(1, GREATEST(1, :seed_count::INT)) AS gs
)
INSERT INTO suppliers (country_id, name, contact_email, contact_phone)
SELECT
  c.id,
  format('Поставщик %s', src.n),
  format('supplier%s@example.com', src.n),
  format('+1000000%s', src.n)
FROM src
JOIN LATERAL (
  SELECT id
  FROM countries
  ORDER BY id
  OFFSET ((src.n - 1) % (SELECT COUNT(*) FROM countries))
  LIMIT 1
) AS c ON TRUE
ON CONFLICT (name) DO NOTHING;

WITH src AS (
  SELECT gs AS n
  FROM generate_series(1, GREATEST(1, :seed_count::INT)) AS gs
)
INSERT INTO warehouses (country_id, name, address_line, city, postal_code)
SELECT
  c.id,
  format('Склад %s', src.n),
  format('%s Адрес', src.n),
  format('Город %s', src.n),
  lpad(src.n::TEXT, 5, '0')
FROM src
JOIN LATERAL (
  SELECT id
  FROM countries
  ORDER BY id
  OFFSET ((src.n - 1) % (SELECT COUNT(*) FROM countries))
  LIMIT 1
) AS c ON TRUE
ON CONFLICT (name) DO NOTHING;

INSERT INTO pickup_points (warehouse_id, name, city, address_line, postal_code, working_hours)
SELECT
  w.id,
  format('Точка выдачи %s', w.id),
  w.city,
  format('%s Адрес', w.id),
  w.postal_code,
  '10:00-22:00'
FROM warehouses AS w
ON CONFLICT (warehouse_id, name) DO NOTHING;

WITH src AS (
  SELECT gs AS n
  FROM generate_series(1, GREATEST(1, :seed_count::INT * 2)) AS gs
)
INSERT INTO purchase_orders (supplier_id, warehouse_id, status, ordered_at, expected_at)
SELECT
  s.id,
  w.id,
  'drafting'::purchase_order_status,
  NOW() - make_interval(days => src.n),
  NOW() + make_interval(days => (src.n % 7 + 1))
FROM src
JOIN LATERAL (
  SELECT id
  FROM suppliers
  ORDER BY id
  OFFSET ((src.n - 1) % (SELECT COUNT(*) FROM suppliers))
  LIMIT 1
) AS s ON TRUE
JOIN LATERAL (
  SELECT id
  FROM warehouses
  ORDER BY id
  OFFSET ((src.n - 1) % (SELECT COUNT(*) FROM warehouses))
  LIMIT 1
) AS w ON TRUE
WHERE NOT EXISTS (
  SELECT 1
  FROM purchase_orders po
  WHERE po.supplier_id = s.id
    AND po.warehouse_id = w.id
    AND po.ordered_at::DATE = (NOW() - make_interval(days => src.n))::DATE
);

WITH ranked_orders AS (
  SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
  FROM purchase_orders
),
ranked_variants AS (
  SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
  FROM product_variants
)
INSERT INTO purchase_order_items (purchase_order_id, variant_id, quantity, unit_cost)
SELECT
  ro.id,
  rv.id,
  20 + (ro.rn % 15),
  ROUND((70 + (rv.rn % 20) * 2.5)::NUMERIC, 2)
FROM ranked_orders ro
JOIN LATERAL (
  SELECT id, rn
  FROM ranked_variants
  ORDER BY rn
  OFFSET ((ro.rn - 1) % (SELECT COUNT(*) FROM ranked_variants))
  LIMIT 1
) AS rv ON TRUE
ON CONFLICT (purchase_order_id, variant_id) DO NOTHING;

INSERT INTO inventory_batches (
  variant_id,
  warehouse_id,
  purchase_order_item_id,
  lot_code,
  quantity_on_hand,
  wholesale_cost,
  produced_on,
  expires_on
)
SELECT
  poi.variant_id,
  po.warehouse_id,
  poi.id,
  format('Лот-%s', poi.id),
  poi.quantity,
  poi.unit_cost,
  CURRENT_DATE - (((poi.id % 20) + 1)::INT),
  CURRENT_DATE + (((poi.id % 180) + 30)::INT)
FROM purchase_order_items poi
JOIN purchase_orders po ON po.id = poi.purchase_order_id
ON CONFLICT (warehouse_id, variant_id, lot_code) DO NOTHING;
