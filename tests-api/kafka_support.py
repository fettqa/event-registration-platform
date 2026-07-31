import json
import os
import time
import uuid
from typing import Any

TOPIC = os.getenv("KAFKA_REGISTRATION_TOPIC", "registration.created")
BOOTSTRAP = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")


def _kafka_modules():
  try:
    from kafka import KafkaConsumer
    from kafka.errors import KafkaError
    from kafka.serializer import DeserializeWrapper
  except ImportError as exc:
    raise ImportError(
        "kafka-python is required for Kafka tests (use >=3.0.8 on Python 3.14). "
        "Run: python -m pip install -U 'kafka-python>=3.0.8'"
    ) from exc
  return KafkaConsumer, KafkaError, DeserializeWrapper


def kafka_available(bootstrap: str = BOOTSTRAP, timeout_ms: int = 3000) -> bool:
  KafkaConsumer, KafkaError, _ = _kafka_modules()
  try:
    consumer = KafkaConsumer(
        bootstrap_servers=bootstrap,
        request_timeout_ms=max(timeout_ms, 1000),
    )
    consumer.close()
    return True
  except (KafkaError, OSError, TimeoutError):
    return False


def wait_for_registration_event(
    *,
    email: str,
    event_id: int,
    bootstrap: str = BOOTSTRAP,
    topic: str = TOPIC,
    timeout_sec: float = 15.0,
) -> dict[str, Any]:
  """Poll topic until a RegistrationCreatedEvent matches email + eventId."""
  KafkaConsumer, _, DeserializeWrapper = _kafka_modules()
  group_id = f"pytest-registration-kafka-{uuid.uuid4().hex[:8]}"
  consumer = KafkaConsumer(
      topic,
      bootstrap_servers=bootstrap,
      group_id=group_id,
      enable_auto_commit=False,
      auto_offset_reset="earliest",
      value_deserializer=DeserializeWrapper(lambda data: json.loads(data.decode("utf-8"))),
      consumer_timeout_ms=1000,
  )

  deadline = time.time() + timeout_sec
  try:
    while time.time() < deadline:
      for record in consumer:
        value = record.value
        if (
            isinstance(value, dict)
            and value.get("email") == email
            and value.get("eventId") == event_id
        ):
          return value
        if time.time() >= deadline:
          break
    raise TimeoutError(
        f"No Kafka message for email={email} eventId={event_id} within {timeout_sec}s"
    )
  finally:
    consumer.close()
