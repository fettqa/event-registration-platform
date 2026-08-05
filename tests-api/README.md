# API QA (black-box)

External HTTP tests against a **running** Spring app (`BASE_URL`).

| Module | Stack | Path |
|--------|-------|------|
| Python | pytest + httpx | [`python/`](python/README.md) |
| Java | JUnit 5 + RestAssured | [`java/`](java/README.md) |

Same contract as Android / web E2E. Domain matrix (409 seats, roles, concurrency) lives here — not in UI suites.

## Shared preconditions

```bash
cd app
./gradlew bootRun
# Kafka + mail scenarios:
docker compose up -d kafka mailpit
./gradlew bootRun --args='--spring.profiles.active=kafka,mail'
```

Admin: `admin@example.com` / `admin123`
