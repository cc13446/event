package com.chenchen.event.verticle;

import com.chenchen.event.service.NamespaceService;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerVerticle extends AbstractVerticle {

  private static final Logger logger= LoggerFactory.getLogger(ManagerVerticle.class);

  private static final int PORT = 8888;

  private Router getRouter() {
    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());

    // hello
    router.get("/hello").handler(req -> req.response()
      .putHeader("content-type", "text/plain")
      .end("hello event!"));

    // create namespace
    router.post("/namespace/create").handler(req -> NamespaceService.INS.create(req, vertx));
    return router;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    // start http server
    Future<HttpServer> httpServerFuture = vertx.createHttpServer().requestHandler(getRouter()).listen(PORT);
    httpServerFuture.andThen(http -> {
      if (http.succeeded()) {
        startPromise.complete();
        logger.info("Http server listen on port {}", PORT);
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
