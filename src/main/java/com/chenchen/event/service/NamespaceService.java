package com.chenchen.event.service;

import com.chenchen.event.dto.NamespaceCreateDTO;
import com.chenchen.event.dto.NamespaceDeleteDTO;
import com.chenchen.event.namespace.NamespaceManager;
import com.chenchen.event.verticle.NamespaceVerticle;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamespaceService {
  private static final Logger logger = LoggerFactory.getLogger(NamespaceService.class);

  private NamespaceService() {
  }

  private static void requestSuccess(RoutingContext req, String body) {
    req.response().putHeader("content-type", "text/plain").end(body);
  }

  private static void requestFail(RoutingContext req, String message) {
    req.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
      .putHeader("content-type", "text/plain")
      .end(message);
  }

  @Data
  private static class NamespaceCreate {
    private NamespaceCreateDTO dto;
    private String verticleId;
  }

  public static void create(RoutingContext req, Vertx vertx) {

    // trans to pojo
    NamespaceCreateDTO namespaceCreateDTO = req.body().asPojo(NamespaceCreateDTO.class);
    logger.info("Receive create namespace [{}]", namespaceCreateDTO);

    // trans
    Future<NamespaceCreate> future = Future.succeededFuture(namespaceCreateDTO).compose(dto -> {
      NamespaceCreate create = new NamespaceCreate();
      create.setDto(dto);
      return Future.succeededFuture(create);
    });

    // check namespace available
    Future<NamespaceCreate> checkNameSpaceFuture = future.compose(create -> {
      String ns = create.getDto().getNamespace();
      return vertx.executeBlocking(promise -> {
        if (NamespaceManager.preCreateNamespace(ns)) {
          logger.info("Namespace [{}] is available", ns);
          promise.complete(create);
        } else {
          logger.info("Namespace [{}] has been created", ns);
          promise.fail("Namespace [" + ns + "] has been created");
        }
      });
    });

    // check success, create namespace
    Future<NamespaceCreate> createNamespaceFuture = checkNameSpaceFuture.compose(create -> {
      String ns = create.getDto().getNamespace();
      // start namespace vertical
      DeploymentOptions deploymentOptions = new DeploymentOptions()
        .setWorker(true)
        .setInstances(1)
        .setWorkerPoolSize(1)
        .setConfig(new JsonObject().put(NamespaceManager.NAMESPACE, ns))
        .setWorkerPoolName("namespace-thread-" + ns);

      logger.info("Start namespace [{}] verticle", ns);
      return vertx.deployVerticle(NamespaceVerticle.class, deploymentOptions).compose(success -> {
        create.setVerticleId(success);
        logger.info("Success start namespace [{}] verticle id [{}]", ns, success);
        return Future.succeededFuture(create);
      });
    });

    // create success, update namespace manager
    Future<NamespaceCreate> updateNamespaceManagerFuture = createNamespaceFuture.compose(create -> {
      String ns = create.getDto().getNamespace();
      String id = create.getVerticleId();
      logger.info("Success to create namespace [{}] verticle id [{}]", ns, id);
      return vertx.executeBlocking(promise -> {
        if (NamespaceManager.updateNamespace(ns, id)) {
          logger.info("Namespace [{}] update namespace manager success id [{}]", ns, id);
          promise.complete(create);
        } else {
          // should be roll back
          logger.info("Namespace [{}] update namespace manager fail id [{}]", ns, id);
          logger.info("Roll back create namespace [{}] verticle id [{}]", ns, id);
          Future<Void> rollBackFuture = vertx.undeploy(id);
          rollBackFuture.onSuccess(r -> {
            logger.info("Roll back verticle id [{}] success", r);
            promise.fail("Namespace [" + ns + "] update namespace manager fail id [ " + r + "] and roll back success");
          });
          rollBackFuture.onFailure(r -> {
            logger.warn("Roll back verticle id [{}] fail : {}", id, r.getMessage());
            promise.fail("Namespace [" + ns + "] update namespace manager fail id [ " + id + "] and roll back fail : " + r.getMessage());
          });
        }
      });
    });

    // update success
    updateNamespaceManagerFuture.onSuccess(create -> {
      logger.info("Create namespace verticle success, id {}", create.getVerticleId());
      requestSuccess(req, create.getVerticleId());
    });

    // update fail
    updateNamespaceManagerFuture.onFailure(fail -> {
      logger.info("Create namespace verticle fail : {}", fail.getMessage());
      requestFail(req, fail.getMessage());
    });
  }

  @Data
  private static class NamespaceDelete {
    private NamespaceDeleteDTO dto;
    private String verticleId;
  }

  public static void delete(RoutingContext req, Vertx vertx) {

    // trans to pojo
    NamespaceDeleteDTO namespaceDeleteDTO = req.body().asPojo(NamespaceDeleteDTO.class);
    logger.info("Receive delete namespace [{}]", namespaceDeleteDTO);

    // trans
    Future<NamespaceDelete> future = Future.succeededFuture(namespaceDeleteDTO).compose(dto -> {
      NamespaceDelete delete = new NamespaceDelete();
      delete.setDto(dto);
      return Future.succeededFuture(delete);
    });

    // get verticle id
    Future<NamespaceDelete> getVerticleIdFuture = future.compose(delete -> {
      String ns = delete.getDto().getNamespace();
      return vertx.executeBlocking(promise -> {
        String verticleId = NamespaceManager.getNamespaceVerticleId(ns);
        if (ObjectUtils.isEmpty(verticleId) || verticleId.equals(NamespaceManager.FAKE_ID)) {
          logger.info("Namespace [{}] has not been created", ns);
          promise.fail("Namespace [" + ns + "] has not been created");
        } else {
          logger.info("Namespace [{}] could be delete id [{}]", ns, verticleId);
          delete.setVerticleId(verticleId);
          promise.complete(delete);
        }
      });
    });

    // get verticle id success undeploy verticle
    Future<NamespaceDelete> undeployVerticle = getVerticleIdFuture.compose(delete -> {
      String ns = delete.getDto().getNamespace();
      String id = delete.getVerticleId();
      // undeploy namespace vertical
      logger.info("undeploy namespace [{}] verticle id [{}]", ns, id);
      return vertx.undeploy(id).compose(success -> {
        logger.info("Success undeploy namespace [{}] verticle id [{}]", ns, id);
        return Future.succeededFuture(delete);
      });
    });

    // update namespace manager
    Future<NamespaceDelete> updateNamespaceManagerFuture = undeployVerticle.compose(delete -> {
      String ns = delete.getDto().getNamespace();
      String id = delete.getVerticleId();
      logger.info("Success to delete namespace [{}] verticle id [{}]", ns, id);
      return vertx.executeBlocking(promise -> {
        if (NamespaceManager.removeNamespace(ns, id)) {
          logger.info("Namespace [{}] remove namespace manager success id [{}]", ns, id);
          promise.complete(delete);
        } else {
          logger.info("Namespace [{}] remove namespace manager success id [{}] fail", ns, id);
          promise.fail("Namespace [" + ns + "] remove namespace manager success id [" + id + "] fail");
        }
      });
    });

    // update success
    updateNamespaceManagerFuture.onSuccess(delete -> {
      logger.info("Delete namespace verticle success, id {}", delete.getVerticleId());
      requestSuccess(req, delete.getVerticleId());
    });

    // update fail
    updateNamespaceManagerFuture.onFailure(fail -> {
      logger.info("Delete namespace verticle fail : {}", fail.getMessage());
      requestFail(req, fail.getMessage());
    });
  }
}
