import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  vus: 2,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],      // < 1% exceptions
    http_req_duration: ['p(95)<500'],  // 95% requests < 500ms
  },
};

export default function () {
  // 1) Health
  const health = http.get(`${BASE_URL}/actuator/health`);
  check(health, {
    'health is 200': (r) => r.status === 200,
  });

  // 2) Create event
  const eventRes = http.post(
      `${BASE_URL}/api/events`,
      JSON.stringify({
        name: `Smoke Event ${__VU}-${__ITER}`,
        maxSeats: 100,
      }),
      { headers: { 'Content-Type': 'application/json' } }
  );

  check(eventRes, {
    'create event is 201': (r) => r.status === 201,
  });

  const eventId = eventRes.json('id');

  // 3) Register for event
  const regRes = http.post(
      `${BASE_URL}/api/events/${eventId}/registrations`,
      JSON.stringify({
        email: `user_${__VU}_${__ITER}@example.com`,
        fullName: `User ${__VU}`,
      }),
      { headers: { 'Content-Type': 'application/json' } }
  );

  check(regRes, {
    'register is 201': (r) => r.status === 201,
  });

  sleep(1);
}