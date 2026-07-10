package com.fettqa.events.registration;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

  long countByEventId(Long eventId);

  boolean existsByEventIdAndEmailIgnoreCase(Long eventId, String email);

}
