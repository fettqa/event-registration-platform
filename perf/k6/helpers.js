import http from 'k6/http';
import { check } from 'k6';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const jsonHeaders = {
  headers: { 'Content-Type': 'application/json' },
};

/**
 * Creates an event for load/spike setup.
 * Fails the test if creation is not 201.
 */
export function createEvent(namePrefix, maxSeats = 100000) {
  const res = http.post(
      `${BASE_URL}/api/events`,
      JSON.stringify({
        name: `${namePrefix} ${Date.now()}`,
        maxSeats: maxSeats,
      }),
      jsonHeaders
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
      jsonHeaders
  );
}