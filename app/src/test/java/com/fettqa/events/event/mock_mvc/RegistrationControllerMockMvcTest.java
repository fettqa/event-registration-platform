package com.fettqa.events.event.mock_mvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fettqa.events.ExceptionHandler;
import com.fettqa.events.registration.RegistrationController;
import com.fettqa.events.registration.RegistrationService;
import com.fettqa.events.registration.dto.EventRegistrationRequest;
import com.fettqa.events.registration.dto.EventRegistrationResponse;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = RegistrationController.class)
@Import(ExceptionHandler.class)
public class RegistrationControllerMockMvcTest {

  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  private RegistrationService registrationService;


  @Test
  void create_returns201() throws Exception {
    when(registrationService.register(eq(1L), any(EventRegistrationRequest.class)))
        .thenReturn(new EventRegistrationResponse(1L, 1L, "john@example.com", "John Doe",
            OffsetDateTime.now()));

    mockMvc.perform(post("/api/events/1/registrations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"john@example.com","fullName":"John Doe"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("john@example.com"));
  }

  @Test
  void registrationInvalidEmail_returns400() throws Exception {
    mockMvc.perform(post("/api/events/1/registrations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"invalid-email","fullName":"John Doe"}
                """))
        .andExpect(status().isBadRequest());
  }

}
