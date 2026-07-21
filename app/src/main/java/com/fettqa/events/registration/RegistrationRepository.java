package com.fettqa.events.registration;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

  long countByEventId(Long eventId);

  boolean existsByEventIdAndEmailIgnoreCase(Long eventId, String email);

  List<Registration> findByEventId(Long eventId);

  Page<Registration> findByEventId(Long eventId, Pageable pageable);

  Page<Registration> findByEventIdAndFullNameContainingIgnoreCase(
      Long eventId, String fullName, Pageable pageable);
}
