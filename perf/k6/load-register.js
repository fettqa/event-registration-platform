import {check, sleep} from 'k6';
import {createEvent, register} from "./helpers.js";

export const options = {
  stages: [
    {duration: '30s', target: 50}, // Ramp-up to 50 users
    {duration: '2m', target: 50}, // Stay at 50 users for 2 minutes
    {duration: '30s', target: 0}, // Ramp-down to 0 users
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],      // < 5% exceptions
    http_req_duration: ['p(95)<800'],  // 95% requests < 800ms
    checks: ['rate>0.95'] // 95% checks ok
  },
};

export function setup() {
  return createEvent(`Load Test Event`, 100000);
}

export default function (data) {
  const email = `load_u${__VU}_i${__ITER}_d${Date.now()}@example.com`;

  const res = register(data.eventId, email, `Load user ${__VU}`);

  check(res, {
    'registration is 201': (r) => r.status === 201,
  });

  sleep(0.5);
}

