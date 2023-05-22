package com.chenchen.event.verticle;

import com.chenchen.event.config.Config;
import com.chenchen.event.entity.Event;
import com.chenchen.event.entity.codec.EventCodec;
import com.chenchen.event.service.EventService;
import com.chenchen.event.service.NamespaceService;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerVerticle extends AbstractVerticle {

  private static final Logger logger= LoggerFactory.getLogger(ManagerVerticle.class);

  private Router getRouter() {
    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());

    // hello
    router.get("/hello").handler(req -> req.response()
      .putHeader("Content-Type", "text/plain")
      .end("hello event!"));

    // create namespace
    router.post("/namespace/create").handler(req -> NamespaceService.create(req, vertx));

    // delete namespace
    router.post("/namespace/delete").handler(req -> NamespaceService.delete(req, vertx));

    // publish event
    router.post("/event/publish").handler(req -> EventService.publish(req, vertx));

    return router;

  }

  @Override
  public void start(Promise<Void> startPromise) {

    // register codec
    logger.info("Register event codec");
    vertx.eventBus().registerDefaultCodec(Event.class, new EventCodec());

    // config
    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("file")
      .setFormat("yaml")
      .setConfig(new JsonObject().put("path", "config.yaml"));

    ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(store));
    retriever.getConfig().onComplete(json -> {
      if (json.succeeded()) {
        logger.info("Update config");
        Config.INS.updateConfig(json.result());
      } else {
        logger.error("Get config fail {}", json.cause().getMessage());
        startPromise.fail(json.cause());
      }
    }).onSuccess(noUse -> {
      // start http server
      int port = Config.INS.getServer().getPort();
      vertx.createHttpServer().requestHandler(getRouter()).listen(port).onComplete(http -> {
        if (http.succeeded()) {
          logger.info("Http server listen on port [{}]", port);
          startPromise.complete();
        } else {
          logger.error("Create http server fail {}", http.cause().getMessage());
          startPromise.fail(http.cause());
        }
      });
    });
  }
}
