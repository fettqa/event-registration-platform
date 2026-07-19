package com.fettqa.events.registration;

import com.fettqa.events.event.Event;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(name = "registrations",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_registrations_event_email",
        columnNames = {"event_id", "email"}
    )
)
public class Registration {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Event event;

  @Column(nullable = false)
  private String email;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  public Registration(Event event, String fullName, String email) {
    this.event = event;
    this.fullName = fullName;
    this.email = email;
    this.createdAt = OffsetDateTime.now();
  }

  public Registration() {

  }

  public Long getId() {
    return id;
  }

  public Event getEvent() {
    return event;
  }

  public String getFullName() {
    return fullName;
  }

  public String getEmail() {
    return email;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

}
