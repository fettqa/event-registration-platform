package com.fettqa.events.event;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface EventRepository extends JpaRepository<Event, Long> {

  List<Event> findByName(String name);

  /**
   * Loads event and locks the row until the surrounding transaction ends (SELECT … FOR UPDATE).
   * Prevents concurrent registrations from both passing the seat-limit check.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Event e where e.id = :id")
  Optional<Event> findByIdForUpdate(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query(value = "ALTER TABLE events ALTER COLUMN id RESTART WITH 1", nativeQuery = true)
  void resetIdentity();
}