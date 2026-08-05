# API tests — Python (pytest + httpx)

```bash
cd tests-api/python
python -m venv .venv
# Windows: .\.venv\Scripts\activate
pip install -r requirements.txt

# app must be running
pytest
pytest -m "not kafka and not mail"
```

Env: `BASE_URL` (default `http://localhost:8080`), `KAFKA_BOOTSTRAP_SERVERS`, `MAILPIT_URL`.
