import http from 'k6/http';
import { check } from 'k6';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const ADMIN_EMAIL = __ENV.ADMIN_EMAIL || 'admin@example.com';
export const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || 'admin123';

export const jsonHeaders = {
  headers: { 'Content-Type': 'application/json' },
};

/** Cached per k6 JS VM (setup VM or each VU). */
let cachedAdminToken = null;

/**
 * Login once per VM, then reuse the token.
 * Prefer passing the token from setup() into VUs to avoid BCrypt on every VU.
 */
export function loginAdmin() {
  if (cachedAdminToken) {
    return cachedAdminToken;
  }

  const login = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({ email: ADMIN_EMAIL, password: ADMIN_PASSWORD }),
      {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'admin_login' },
      }
  );
  check(login, { 'admin login is 200': (r) => r.status === 200 });
  if (login.status !== 200) {
    throw new Error(`Admin login failed: ${login.status} ${login.body}`);
  }

  cachedAdminToken = login.json('accessToken');
  return cachedAdminToken;
}

function adminHeaders(token) {
  return {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token || loginAdmin()}`,
    },
    tags: { name: 'create_event' },
  };
}

/**
 * Creates an event. Pass token from setup() when possible to skip login in VU loop.
 */
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

  return { eventId: res.json('id') };
}

export function register(eventId, email, fullName) {
  return http.post(
      `${BASE_URL}/api/events/${eventId}/registrations`,
      JSON.stringify({ email, fullName }),
      {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'register' },
      }
  );
}
