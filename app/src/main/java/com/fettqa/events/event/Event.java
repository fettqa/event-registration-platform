package com.fettqa.events.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(name = "events",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_events_name",
        columnNames = {"name"}
    )
)
public class Event {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(name = "max_seats", nullable = false)
  private Integer maxSeats;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected Event() {
  }

  public Event(String name, Integer maxSeats) {
    this.name = name;
    this.maxSeats = maxSeats;
    this.createdAt = OffsetDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Integer getMaxSeats() {
    return maxSeats;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setMaxSeats(Integer integer) {
    this.maxSeats = integer;
  }

  public void setName(String name) {
    this.name = name;
  }
}