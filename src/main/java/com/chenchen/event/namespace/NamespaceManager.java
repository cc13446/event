package com.chenchen.event.namespace;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NamespaceManager {

  public static final String NAMESPACE = "namespace";


  // key namespace
  // value verticle id
  private static final Map<String, String> namespaceMap = new ConcurrentHashMap<>();

  public static boolean hasNamespace(String namespace) {
    return namespaceMap.containsKey(namespace);
  }

  public static boolean putNamespace(String namespace, String id) {
      return namespaceMap.putIfAbsent(namespace, id) == null;
  }

}
