package com.fettqa.events.api.support;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public final class KafkaSupport {

  private KafkaSupport() {
  }

  public static String bootstrapServers() {
    return System.getProperty(
        "kafkaBootstrapServers",
        System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
  }

  public static boolean available() {
    try (Consumer<String, String> consumer = newConsumer()) {
      return !consumer.listTopics(Duration.ofSeconds(2)).isEmpty();
    } catch (Exception ex) {
      return false;
    }
  }

  public static Consumer<String, String> newConsumer() {
    return new KafkaConsumer<>(Map.of(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers(),
        ConsumerConfig.GROUP_ID_CONFIG, "api-test-" + UUID.randomUUID(),
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()));
  }

  public static ConsumerRecords<String, String> pollUntilRecord(
      Consumer<String, String> consumer, Duration timeout) {
    long deadline = System.nanoTime() + timeout.toNanos();
    while (System.nanoTime() < deadline) {
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
      if (!records.isEmpty()) {
        return records;
      }
    }
    throw new AssertionError("No Kafka record received within " + timeout);
  }
}
