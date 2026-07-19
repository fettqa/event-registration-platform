package com.fettqa.events.utils;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestDataCleaner {

  private final EntityManager entityManager;

  public TestDataCleaner(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Transactional
  public void cleanAndResetIds() {
    entityManager.createNativeQuery("DELETE FROM registrations").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
    entityManager.createNativeQuery(
        "ALTER TABLE registrations ALTER COLUMN id RESTART WITH 1").executeUpdate();
    entityManager.createNativeQuery(
        "ALTER TABLE events ALTER COLUMN id RESTART WITH 1").executeUpdate();
  }
}