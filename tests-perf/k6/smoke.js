import http from 'k6/http';
import { check, sleep } from 'k6';
import {
  BASE_URL,
  createEvent,
  handleSummaryFor,
  loginAdmin,
  registerForEvent,
  signup,
} from './helpers.js';

export const handleSummary = handleSummaryFor('smoke');

export const options = {
  vus: 2,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
    checks: ['rate>0.95'],
  },
};

export function setup() {
  return { token: loginAdmin() };
}

export default function (data) {
  const health = http.get(`${BASE_URL}/actuator/health`, {
    tags: { name: 'health' },
  });
  check(health, {
    'health is 200': (r) => r.status === 200,
  });

  const eventRes = createEvent(`Smoke Event ${__VU}-${__ITER}`, 10000, data.token);

  const email = `smoke_u${__VU}_i${__ITER}_${Date.now()}@example.com`;
  const userToken = signup(email, `Smoke User ${__VU}`);
  const regRes = registerForEvent(eventRes.eventId, userToken);

  check(regRes, {
    'register is 201': (r) => r.status === 201,
  });

  sleep(1);
}
