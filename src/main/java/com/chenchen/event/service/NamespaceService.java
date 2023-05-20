package com.chenchen.event.service;

import com.chenchen.event.dto.NamespaceCreateDTO;
import com.chenchen.event.namespace.NamespaceManager;
import com.chenchen.event.verticle.ManagerVerticle;
import com.chenchen.event.verticle.NamespaceVerticle;
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

  public static void create(RoutingContext req, Vertx vertx) {
    NamespaceCreateDTO dto = req.body().asPojo(NamespaceCreateDTO.class);
    logger.info("receive create namespace {}", dto);

    // start namespace vertical
    DeploymentOptions deploymentOptions = new DeploymentOptions()
      .setWorker(true)
      .setInstances(1)
      .setWorkerPoolSize(1)
      .setConfig(new JsonObject().put(NamespaceManager.NAMESPACE, dto.getName()))
      .setWorkerPoolName("manager-thread-" + dto.getName());

    Future<String> managerVerticleFuture = vertx.deployVerticle(NamespaceVerticle.class, deploymentOptions);
    managerVerticleFuture.onSuccess(s -> {
      logger.info("create namespace verticle success {}", s);
      req.response().end(s);
    });
  }
}
