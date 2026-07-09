package com.fettqa.events.event;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface EventRepository extends JpaRepository<Event, Long> {

  List<Event> findByName(String name);

  @Modifying
  @Transactional
  @Query(value = "ALTER TABLE events ALTER COLUMN id RESTART WITH 1", nativeQuery = true)
  void resetIdentity();
}