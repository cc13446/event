package com.chenchen.event.entity;


import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Event {

  private static String EVENT = "event";

  private static String MESSAGE = "message";

  private String event;

  private String message;

  public static String encode(Event event) {
    JsonObject jsonToEncode = new JsonObject();
    jsonToEncode.put(EVENT, event.event);
    jsonToEncode.put(MESSAGE, event.message);

    return jsonToEncode.encode();
  }

  public static Event decode(String json) {

    JsonObject contentJson = new JsonObject(json);

    // Get fields
    String event = contentJson.getString(EVENT);
    String message = contentJson.getString(MESSAGE);

    return new Event(event, message);

  }

}
