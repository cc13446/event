package com.chenchen.event.service;

import com.chenchen.event.dto.NamespaceCreateDTO;
import com.chenchen.event.verticle.ManagerVerticle;
import com.chenchen.event.verticle.NamespaceVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamespaceService {
  private static final Logger logger= LoggerFactory.getLogger(NamespaceService.class);

  public static final NamespaceService INS = new NamespaceService();

  private NamespaceService(){}

  public void create(RoutingContext req, Vertx vertx) {
    NamespaceCreateDTO dto = req.body().asPojo(NamespaceCreateDTO.class);
    logger.debug("receive create namespace dto {}", dto);
    createNamespace(dto, req, vertx);
  }



  public void createNamespace(NamespaceCreateDTO namespaceCreateDTO, RoutingContext req, Vertx vertx) {
    // start namespace vertical
    DeploymentOptions deploymentOptions = new DeploymentOptions()
      .setWorker(true)
      .setInstances(1)
      .setWorkerPoolSize(1)
      .setWorkerPoolName("manager-thread");

    Future<String> managerVerticleFuture = vertx.deployVerticle(NamespaceVerticle.class, deploymentOptions);

    managerVerticleFuture.onSuccess(s -> {
      logger.info("create namespace verticle success {}", s);
      req.response().end(s);
    });
  }


}
