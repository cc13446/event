package com.chenchen.event.service;

import com.chenchen.event.verticle.NamespaceVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class NamespaceService {



  public void createNamespace(Vertx vertx, String namespace) {
    // start manager vertical
    DeploymentOptions deploymentOptions = new DeploymentOptions()
      .setWorker(true)
      .setInstances(1)
      .setWorkerPoolSize(1)
      .setWorkerPoolName("manager-thread");

    Future<String> managerVerticleFuture = vertx.deployVerticle(NamespaceVerticle.class, deploymentOptions);
  }
}
