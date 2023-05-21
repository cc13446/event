package com.chenchen.event.entity.codec;

import com.chenchen.event.entity.Event;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class EventCodec implements MessageCodec<Event, Event> {
  @Override
  public void encodeToWire(Buffer buffer, Event event) {
    String jsonToStr = Event.encode(event);
    int length = jsonToStr.getBytes().length;
    buffer.appendInt(length);
    buffer.appendString(jsonToStr);
  }

  @Override
  public Event decodeFromWire(int pos, Buffer buffer) {
    int length = buffer.getInt(pos);
    String json = buffer.getString(pos + 4, pos + 4 + length);
    return Event.decode(json);
  }

  @Override
  public Event transform(Event event) {
    // If a message is sent locally across the event bus.
    return event;
  }

  @Override
  public String name() {
    // Each codec must have a unique name.
    // This is used to identify a codec when sending a message and for unregistering codecs.
    return this.getClass().getSimpleName();
  }

  @Override
  public byte systemCodecID() {
    // Always -1
    return -1;
  }
}
