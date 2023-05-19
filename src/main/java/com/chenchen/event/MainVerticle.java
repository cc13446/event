package com.chenchen.event;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

  private Router getRouter() {
    Router router = Router.router(vertx);

    router.get("/hello").handler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    });

    return router;
  }

  private void started(AsyncResult<HttpServer> http, Promise<Void> startPromise) {
    if (http.succeeded()) {
      startPromise.complete();
      System.out.println("HTTP server started on port 8888");
    } else {
      startPromise.fail(http.cause());
    }
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.createHttpServer().requestHandler(getRouter() ).listen(8888, http -> started(http, startPromise));
  }

}
