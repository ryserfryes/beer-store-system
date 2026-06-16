INSERT INTO product_views (variant_id, customer_id, viewed_at)
SELECT
    pv.id AS variant_id,
    c.id  AS customer_id,
    NOW() - make_interval(hours => (((c.rn - 1) * 3 + pv.rn) % (24 * 90))::INT)
FROM (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM customers
    LIMIT GREATEST(1, :seed_count::INT)
) AS c
JOIN LATERAL (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM product_variants
    ORDER BY id
    OFFSET ((c.rn - 1) % 10) * 3
    LIMIT 3
) AS pv ON TRUE
ON CONFLICT DO NOTHING;
