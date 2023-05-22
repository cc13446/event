package com.chenchen.event;

import com.chenchen.event.config.Config;
import com.chenchen.event.dto.NamespaceCreateDTO;
import com.chenchen.event.namespace.NamespaceManager;
import com.chenchen.event.verticle.ManagerVerticle;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(VertxExtension.class)
public class NamespaceTest {

  @BeforeEach
  void deployVerticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new ManagerVerticle(), testContext.succeedingThenComplete());
  }

  private Future<String> postCreateNamespace(Vertx vertx, String ns) {
    HttpClient client = vertx.createHttpClient();
    return client.request(HttpMethod.POST, Config.INS.getServer().getPort(), "127.0.0.1", "/namespace/create").compose(req -> {
      JsonObject body = new JsonObject();
      body.put("namespace", ns);
      return req.putHeader("Content-Type", "application/json").send(body.encode());
    }).compose(response -> {
      if (response.statusCode() == HttpResponseStatus.OK.code()) {
        return response.body().compose(buffer -> Future.succeededFuture(buffer.toString()));
      }
      return response.body().compose(buffer -> Future.failedFuture("Create namespace fail : " + buffer.toString()));
    });
  }

  @Test
  void createNamespaceSuccess(Vertx vertx, VertxTestContext testContext) {
    String testNs = "testSuccess";
    postCreateNamespace(vertx, testNs).onComplete(testContext.succeeding(res -> testContext.verify(() -> {
      String id = NamespaceManager.getNamespaceVerticleId(testNs);
      assertThat(id).isNotEqualTo(null).isNotEqualTo(NamespaceManager.FAKE_ID).isEqualTo(res);
      testContext.completeNow();
    })));
  }

  @Test
  void createNamespaceFail(Vertx vertx, VertxTestContext testContext) {
    String testNs = "testFail";
    postCreateNamespace(vertx, testNs).compose(res -> {
      String id = NamespaceManager.getNamespaceVerticleId(testNs);
      assertThat(id).isNotEqualTo(null).isNotEqualTo(NamespaceManager.FAKE_ID).isEqualTo(res);
      return Future.succeededFuture(id);
    }).onComplete(id -> {
      assertThat(id.succeeded()).isEqualTo(true);
      postCreateNamespace(vertx, testNs).onComplete(res -> testContext.verify(() -> {
        assertThat(res.succeeded()).isEqualTo(false);
        assertThat(id.result()).isNotEqualTo(null).isNotEqualTo(NamespaceManager.FAKE_ID)
          .isEqualTo(NamespaceManager.getNamespaceVerticleId(testNs));
        testContext.completeNow();
      }));
    });
  }
}
