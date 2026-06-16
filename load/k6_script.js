import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    oltp: {
      executor: 'ramping-vus',
      exec: 'oltpWorkload',
      stages: [
        { duration: '1m', target: 10 },
        { duration: '3m', target: 10 },
        { duration: '30s', target: 0 },
      ],
    },
    olap: {
      executor: 'ramping-vus',
      exec: 'olapWorkload',
      stages: [
        { duration: '1m', target: 3 },
        { duration: '3m', target: 3 },
        { duration: '30s', target: 0 },
      ],
    },
    timeseries: {
      executor: 'ramping-vus',
      exec: 'timeseriesWorkload',
      stages: [
        { duration: '1m', target: 5 },
        { duration: '3m', target: 5 },
        { duration: '30s', target: 0 },
      ],
    },
  },
};

export function oltpWorkload() {
  const page = Math.floor(Math.random() * 5);

  const catalog = http.get(`${BASE_URL}/api/catalog?page=${page}&size=20`);
  check(catalog, { 'catalog 200': (r) => r.status === 200 });

  const orderId = Math.floor(Math.random() * 100) + 1;
  const order = http.get(`${BASE_URL}/api/orders/${orderId}`);
  check(order, { 'order 200': (r) => r.status === 200 });

  const cartPayload = JSON.stringify({
    cart_id: Math.floor(Math.random() * 50) + 1,
    variant_id: Math.floor(Math.random() * 10) + 1,
    quantity: Math.floor(Math.random() * 3) + 1,
  });
  const cart = http.post(`${BASE_URL}/api/cart/items`, cartPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(cart, { 'cart 200': (r) => r.status === 200 });

  const orderPayload = JSON.stringify({
    customer_id: Math.floor(Math.random() * 50) + 1,
    pickup_point_id: Math.floor(Math.random() * 10) + 1,
    items: [
      {
        variant_id: Math.floor(Math.random() * 10) + 1,
        quantity: Math.floor(Math.random() * 3) + 1,
      },
    ],
  });
  const newOrder = http.post(`${BASE_URL}/api/orders`, orderPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(newOrder, { 'create order 200': (r) => r.status === 200 });

  sleep(0.5);
}

export function olapWorkload() {
  const years = ['2024', '2025', '2026'];
  const from = years[Math.floor(Math.random() * years.length)] + '-01-01';
  const to   = years[Math.floor(Math.random() * years.length)] + '-12-31';

  const revenue = http.get(`${BASE_URL}/api/analytics/revenue?from=${from}&to=${to}`);
  check(revenue, { 'revenue 200': (r) => r.status === 200 });

  sleep(2);
}

export function timeseriesWorkload() {
  const variantId = Math.floor(Math.random() * 10) + 1;

  const view = http.post(`${BASE_URL}/api/catalog/${variantId}/view`, null, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(view, { 'view 200': (r) => r.status === 200 });

  sleep(0.2);
}
