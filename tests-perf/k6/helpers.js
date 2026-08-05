import http from 'k6/http';
import {check} from 'k6';
import {textSummary} from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';
import {
  htmlReport
} from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const ADMIN_EMAIL = __ENV.ADMIN_EMAIL || 'admin@example.com';
export const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || 'admin123';

let cachedAdminToken = null;

export function loginAdmin() {
  if (cachedAdminToken) {
    return cachedAdminToken;
  }

  const login = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({email: ADMIN_EMAIL, password: ADMIN_PASSWORD}),
      {
        headers: {'Content-Type': 'application/json'},
        tags: {name: 'admin_login'},
      }
  );
  check(login, {'admin login is 200': (r) => r.status === 200});
  if (login.status !== 200) {
    throw new Error(`Admin login failed: ${login.status} ${login.body}`);
  }

  cachedAdminToken = login.json('accessToken');
  return cachedAdminToken;
}

export function signup(email, fullName, password = 'secret12') {
  const res = http.post(
      `${BASE_URL}/api/auth/register`,
      JSON.stringify({fullName, email, password}),
      {
        headers: {'Content-Type': 'application/json'},
        tags: {name: 'auth_register'},
      }
  );
  check(res, {'auth register is 201': (r) => r.status === 201});
  if (res.status !== 201) {
    throw new Error(`Auth register failed: ${res.status} ${res.body}`);
  }
  return res.json('accessToken');
}

function adminHeaders(token) {
  return {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token || loginAdmin()}`,
    },
    tags: {name: 'create_event'},
  };
}

export function createEvent(namePrefix, maxSeats = 100000, token) {
  const res = http.post(
      `${BASE_URL}/api/events`,
      JSON.stringify({
        name: `${namePrefix} ${Date.now()}`,
        maxSeats: maxSeats,
      }),
      adminHeaders(token)
  );

  check(res, {
    'event creation is 201': (r) => r.status === 201,
  });

  if (res.status !== 201) {
    throw new Error(`Failed to create event: ${res.status} ${res.body}`);
  }

  return {eventId: res.json('id')};
}

export function registerForEvent(eventId, token) {
  return http.post(
      `${BASE_URL}/api/events/${eventId}/registrations`,
      null,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
        tags: {name: 'register'},
      }
  );
}

export function handleSummaryFor(name) {
  return function handleSummary(data) {
    return {
      stdout: textSummary(data, {indent: ' ', enableColors: true}),
      [`tests-perf/k6/results/${name}-summary.json`]: JSON.stringify(data, null, 2),
      [`tests-perf/k6/results/${name}-report.html`]: htmlReport(data),
    };
  };
}

