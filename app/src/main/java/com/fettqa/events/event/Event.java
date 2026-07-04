package com.fettqa.events.event;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "events")
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
}