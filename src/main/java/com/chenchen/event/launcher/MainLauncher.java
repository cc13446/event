package com.chenchen.event.launcher;

import io.vertx.core.Launcher;

public class MainLauncher extends Launcher {
  public static void main(String[] args) {
    // start main vertical
    new MainLauncher().dispatch(args);
  }
}
