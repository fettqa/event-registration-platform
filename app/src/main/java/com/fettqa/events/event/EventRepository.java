package com.fettqa.events.event;

import com.fettqa.events.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}