package com.chenchen.event.service;

import com.chenchen.event.dto.EventPublishDTO;
import com.chenchen.event.entity.Event;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventService {
  private static final Logger logger = LoggerFactory.getLogger(EventService.class);

  private EventService(){}

  public static void publish(RoutingContext req, Vertx vertx) {
    EventPublishDTO dto = req.body().asPojo(EventPublishDTO.class);
    vertx.eventBus().request(dto.getNamespace(), new Event(dto.getEvent(), dto.getMessage()), res -> {
      req.response().end((String) res.result().body());
    });
  }
}
