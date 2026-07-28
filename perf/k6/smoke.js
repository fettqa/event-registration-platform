import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, createEvent, loginAdmin, register } from './helpers.js';

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

  const regRes = register(
      eventRes.eventId,
      `user_${__VU}_${__ITER}@example.com`,
      `User ${__VU}`
  );

  check(regRes, {
    'register is 201': (r) => r.status === 201,
  });

  sleep(1);
}
