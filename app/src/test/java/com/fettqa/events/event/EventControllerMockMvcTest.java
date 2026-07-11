package com.fettqa.events.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fettqa.events.ExceptionHandler;
import com.fettqa.events.event.dto.CreateEventRequest;
import com.fettqa.events.event.dto.EventResponse;
import com.fettqa.events.event.dto.UpdateEventRequest;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EventController.class)
@Import(ExceptionHandler.class)
class EventControllerMockMvcTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private EventService eventService;

  @Test
  void createEvent_returns201() throws Exception {
    when(eventService.create(any(CreateEventRequest.class)))
        .thenReturn(new EventResponse(42L, "QA Conf", 50, OffsetDateTime.now()));

    mockMvc.perform(post("/api/events")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"QA Conf","maxSeats":50}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(42))
        .andExpect(jsonPath("$.name").value("QA Conf"));
  }

  @Test
  void createEvent_returns400() throws Exception {
    mockMvc.perform(post("/api/events")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"QA Conf","maxSeats":-1}
                """))
        .andExpect(status().isBadRequest());

    mockMvc.perform(post("/api/events")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"", "maxSeats":  1}
                """))
        .andExpect(status().isBadRequest());

    mockMvc.perform(post("/api/events")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"maxSeats":  10}
                """))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(eventService);
  }

  @Test
  void event_returns404() throws Exception {
    when(eventService.getById(999L))
        .thenThrow(new EventNotFoundException("event with id: 999 not found"));
    when(eventService.updateById(eq(999L), any(UpdateEventRequest.class)))
        .thenThrow(new EventNotFoundException("event with id: 999 not found"));
    doThrow(new EventNotFoundException("event with id: 999 not found"))
        .when(eventService).delete(999L);

    mockMvc.perform(get("/api/events/999"))
        .andExpect(status().isNotFound());
    mockMvc.perform(patch("/api/events/999")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"QA Conf","maxSeats":50}
                """))
        .andExpect(status().isNotFound());
    mockMvc.perform(delete("/api/events/999"))
        .andExpect(status().isNotFound());
  }
}
