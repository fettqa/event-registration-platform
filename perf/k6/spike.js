import {check, sleep} from 'k6';
import {createEvent, register} from "./helpers.js";

export const options = {
  stages: [
    {duration: '10s', target: 10}, // Ramp-up to 10 users
    {duration: '20s', target: 100}, // Ramp-up to 100 users within 20 seconds
    {duration: '30s', target: 100}, // Stay at 100 users for 30 seconds
    {duration: '10s', target: 0}, // Ramp-down to 0 users
  ],
  thresholds: {
    http_req_failed: ['rate<0.10'],      // < 10% exceptions
    http_req_duration: ['p(95)<1500'],  // 95% requests < 1500ms
    checks: ['rate>0.95']
  },
};

export function setup() {
  return createEvent(`Spike Test Event`, 100000);
}

export default function (data) {
  const email = `spike_u${__VU}_i${__ITER}_d${Date.now()}@example.com`;

  const res = register(data.eventId, email, `Spike user ${__VU}`);

  check(res, {
    'registration is 201': (r) => r.status === 201,
  });

  sleep(0.3);
}