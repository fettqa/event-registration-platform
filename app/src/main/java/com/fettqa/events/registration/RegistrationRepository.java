package com.fettqa.events.registration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

  long countByEventId(Long eventId);

  boolean existsByEventIdAndEmailIgnoreCase(Long eventId, String email);

  @Modifying
  @Transactional
  @Query(value = "ALTER TABLE registrations ALTER COLUMN id RESTART WITH 1", nativeQuery = true)
  void resetIdentity();

}
