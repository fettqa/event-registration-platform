import {check, sleep} from 'k6';
import {createEvent, handleSummaryFor, registerForEvent, signup} from './helpers.js';

export const handleSummary = handleSummaryFor('load');

export const options = {
  stages: [
    {duration: '30s', target: 50},
    {duration: '2m', target: 50},
    {duration: '30s', target: 0},
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<800'],
    checks: ['rate>0.95'],
  },
};

export function setup() {
  return createEvent(`Load Test Event`, 100000);
}

export default function (data) {
  const email = `load_u${__VU}_i${__ITER}_d${Date.now()}@example.com`;
  const token = signup(email, `Load user ${__VU}`);
  const res = registerForEvent(data.eventId, token);

  check(res, {
    'registration is 201': (r) => r.status === 201,
  });

  sleep(0.5);
}
