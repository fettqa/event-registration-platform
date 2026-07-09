package com.fettqa.events;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;

public class Utils {

  public static <T> T jsonToObject(String jsonPath, Class<T> object) {
    InputStream stream = Utils.class.getClassLoader().getResourceAsStream(jsonPath);
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(stream, object);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
