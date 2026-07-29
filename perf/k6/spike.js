import {check, sleep} from 'k6';
import {createEvent, handleSummaryFor, registerForEvent, signup} from './helpers.js';

export const handleSummary = handleSummaryFor('spike');

export const options = {
  stages: [
    {duration: '10s', target: 10},
    {duration: '20s', target: 100},
    {duration: '30s', target: 100},
    {duration: '10s', target: 0},
  ],
  thresholds: {
    http_req_failed: ['rate<0.10'],
    http_req_duration: ['p(95)<1500'],
    checks: ['rate>0.95'],
  },
};

export function setup() {
  return createEvent(`Spike Test Event`, 100000);
}

export default function (data) {
  const email = `spike_u${__VU}_i${__ITER}_d${Date.now()}@example.com`;
  const token = signup(email, `Spike user ${__VU}`);
  const res = registerForEvent(data.eventId, token);

  check(res, {
    'registration is 201': (r) => r.status === 201,
  });

  sleep(0.3);
}
