package com.chenchen.event.verticle;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

public class ManagerVerticle extends AbstractVerticle {

  private Router getRouter() {
    Router router = Router.router(vertx);

    router.get("/hello").handler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    });

    return router;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    // start http server
    Future<HttpServer> httpServerFuture = vertx.createHttpServer().requestHandler(getRouter()).listen(8888);
    httpServerFuture.andThen(http -> {
      if (http.succeeded()) {
        startPromise.complete();
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
