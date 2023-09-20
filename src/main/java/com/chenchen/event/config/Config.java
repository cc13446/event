package com.chenchen.event.config;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class Config {

  private static final Logger logger = LoggerFactory.getLogger(Config.class);

  @Getter
  public static class Server {

    private static final String SERVER_PORT = "server.port";

    // the port listened by server
    private int port;

    private Server() {
    }

  }

  public final static Config INS = new Config();

  private Config() {
  }

  private final Server server = new Server();

  public void updateConfig(JsonObject config) {
    this.server.port = config.getInteger(Server.SERVER_PORT, 8888);
    logger.info("Set config server port [{}]", this.server.port);
  }
}
