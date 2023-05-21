package com.chenchen.event.verticle;

import com.chenchen.event.event.EventManager;
import com.chenchen.event.namespace.NamespaceManager;
import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamespaceVerticle extends AbstractVerticle {

  private static Logger logger = LoggerFactory.getLogger(NamespaceVerticle.class);

  @Override
  public void start() {

    String namespace = config().getString(NamespaceManager.NAMESPACE);

    vertx.eventBus().consumer(namespace, handle -> {
      logger.info("receive message from event bus : {}", handle.body());
      handle.reply(EventManager.EVENT_ARRIVE_NAMESPACE);
    });
  }
}
