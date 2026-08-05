# Java API tests

Standalone JUnit 5 and RestAssured black-box tests for the running event-registration app.
They mirror the Python API suite and never start Spring or access application internals.

## Prerequisites

Start the application first. The target URL is `BASE_URL`, or `http://localhost:8080` by
default. The tests use the seeded default administrator: `admin@example.com` / `admin123`.

```powershell
.\gradlew.bat test
.\gradlew.bat test -DbaseUrl=http://localhost:8080
```

The default task excludes infrastructure-dependent tests. With the app configured to publish
registration messages, run Kafka tests against `KAFKA_BOOTSTRAP_SERVERS` (default
`localhost:9092`):

```powershell
.\gradlew.bat kafkaTest
```

With the app configured to send mail to Mailpit, run mail tests against `MAILPIT_URL`
(default `http://localhost:8025`):

```powershell
.\gradlew.bat mailTest
```
