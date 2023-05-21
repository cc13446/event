package com.chenchen.event;

import com.chenchen.event.verticle.ManagerVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(VertxExtension.class)
public class HelloTest {

  @BeforeEach
  void deployVerticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new ManagerVerticle(), testContext.succeedingThenComplete());
  }

  @Test
  void testHello(Vertx vertx, VertxTestContext testContext) {
    HttpClient client = vertx.createHttpClient();
    client.request(HttpMethod.GET, 8888, "localhost", "/hello")
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
        assertThat(buffer.toString()).isEqualTo("hello event!");
        testContext.completeNow();
      })));
  }

}
