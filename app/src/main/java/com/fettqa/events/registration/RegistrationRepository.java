package com.fettqa.events.registration;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

  long countByEventId(Long eventId);

  boolean existsByEventIdAndEmailIgnoreCase(Long eventId, String email);

  List<Registration> findByEventId(Long eventId);
}
