package com.fettqa.events.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Utils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  public static <T> T jsonToObject(String jsonPath, Class<T> object) {
    InputStream stream = Utils.class.getClassLoader().getResourceAsStream(jsonPath);
    try {
      return OBJECT_MAPPER.readValue(stream, object);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String jsonAsString(String testDataPath) {
    try {
      return new String(
          Objects.requireNonNull(Utils.class.getClassLoader().getResourceAsStream(testDataPath))
              .readAllBytes(),
          StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
