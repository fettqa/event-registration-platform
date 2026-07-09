package com.fettqa.events.event;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventControllerMockMvcTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void createEvent_returns201() throws Exception {
    mockMvc.perform(post("/api/events")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"QA Conf","maxSeats":50}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("QA Conf"));
  }

  @Test
  void patchEvent_returns200() throws Exception {
    String response = mockMvc.perform(post("/api/events")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"QA Conf","maxSeats":50}
                """))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Integer id = JsonPath.read(response, "$.id");
    mockMvc.perform(patch("/api/events/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"QA Conf","maxSeats":50}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("QA Conf"));
    mockMvc.perform(delete("/api/events/" + id))
        .andExpect(status().isNoContent());
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
  }

  @Test
  void event_returns404() throws Exception {
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
