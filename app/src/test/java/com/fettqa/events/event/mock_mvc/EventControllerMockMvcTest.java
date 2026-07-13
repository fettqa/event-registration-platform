package com.fettqa.events.event.mock_mvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fettqa.events.ExceptionHandler;
import com.fettqa.events.event.EventController;
import com.fettqa.events.event.EventService;
import com.fettqa.events.event.dto.CreateEventRequest;
import com.fettqa.events.event.dto.EventResponse;
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
}
