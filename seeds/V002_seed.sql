WITH src AS (
  SELECT gs AS n
  FROM generate_series(1, GREATEST(1, :seed_count::INT * 2)) AS gs
)
INSERT INTO breweries (country_id, name, website_url, founded_year)
SELECT
  c.id,
  format('Варня %s', src.n),
  format('https://brewery%s.example.com', src.n),
  1980 + (src.n % 40)
FROM src
JOIN LATERAL (
  SELECT id
  FROM countries
  ORDER BY id
  OFFSET ((src.n - 1) % (SELECT COUNT(*) FROM countries))
  LIMIT 1
) AS c ON TRUE
ON CONFLICT (country_id, name) DO NOTHING;

WITH src AS (
  SELECT gs AS n
  FROM generate_series(1, GREATEST(1, :seed_count::INT * 10)) AS gs
)
INSERT INTO beers (brewery_id, style_id, name, description, abv)
SELECT
  b.id,
  s.id,
  format('Пиво %s', src.n),
  format('Несуществующее пиво #%s', src.n),
  ROUND((4.2 + (src.n % 10) * 0.55)::NUMERIC, 2)
FROM src
JOIN LATERAL (
  SELECT id
  FROM breweries
  ORDER BY id
  OFFSET ((src.n - 1) % (SELECT COUNT(*) FROM breweries))
  LIMIT 1
) AS b ON TRUE
JOIN LATERAL (
  SELECT id
  FROM beer_styles
  ORDER BY id
  OFFSET ((src.n - 1) % (SELECT COUNT(*) FROM beer_styles))
  LIMIT 1
) AS s ON TRUE
ON CONFLICT (brewery_id, name) DO NOTHING;

INSERT INTO product_variants (beer_id, package_type_id, sku, volume_ml, unit_price)
SELECT
  b.id,
  pt.id,
  format('SKU-%s-%s', b.id, pt.id),
  CASE WHEN pt.code = 'bottle' THEN 500 ELSE 440 END,
  ROUND((110 + (b.id % 30) * 5)::NUMERIC, 2)
FROM beers AS b
JOIN package_types AS pt ON pt.code IN ('can', 'bottle')
ON CONFLICT (sku) DO NOTHING;
