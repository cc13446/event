package com.chenchen.event.service;

import com.chenchen.event.dto.NamespaceCreateDTO;
import com.chenchen.event.namespace.NamespaceManager;
import com.chenchen.event.verticle.NamespaceVerticle;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamespaceService {
  private static final Logger logger= LoggerFactory.getLogger(NamespaceService.class);

  private NamespaceService(){}

  private static void createNamespaceSuccess(RoutingContext req, String id) {
    logger.info("Create namespace verticle success, id {}", id);
    req.response().end(id);
  }

  private static void createNamespaceFail(RoutingContext req, String message) {
    logger.info("Create namespace verticle fail {}", message);
    req.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
      .putHeader("content-type", "text/plain")
      .end("Create namespace verticle fail" + message);
  }

  public static void create(RoutingContext req, Vertx vertx) {
    NamespaceCreateDTO dto = req.body().asPojo(NamespaceCreateDTO.class);
    logger.info("Receive create namespace [{}]", dto);

    // check namespace has
    Future<String> checkNameSpaceFuture = vertx.executeBlocking(promise -> {
      if (NamespaceManager.hasNamespace(dto.getNamespace())) {
        promise.complete("The namespace is available");
      } else {
        promise.fail("The namespace has been created");
      }
    });

    checkNameSpaceFuture.onFailure(fail -> createNamespaceFail(req, fail.getMessage()));

    // create namespace
    checkNameSpaceFuture.onSuccess(res -> {
        // start namespace vertical
        DeploymentOptions deploymentOptions = new DeploymentOptions()
          .setWorker(true)
          .setInstances(1)
          .setWorkerPoolSize(1)
          .setConfig(new JsonObject().put(NamespaceManager.NAMESPACE, dto.getNamespace()))
          .setWorkerPoolName("namespace-thread-" + dto.getNamespace());

        logger.info("Start namespace [{}] verticle", dto.getNamespace());
        Future<String> managerVerticleFuture = vertx.deployVerticle(NamespaceVerticle.class, deploymentOptions);

        // create fail
        managerVerticleFuture.onFailure(fail -> createNamespaceFail(req, fail.getMessage()));

        // create success
        managerVerticleFuture.onSuccess(success -> {
          createNamespaceSuccess(req, success);
        });
    });
  }
}
