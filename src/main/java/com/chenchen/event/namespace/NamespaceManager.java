package com.chenchen.event.namespace;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NamespaceManager {

  public static final String NAMESPACE = "namespace";

  private static final String FAKE_ID = "fake_id";

  // key namespace
  // value verticle id
  private static final Map<String, String> namespaceMap = new ConcurrentHashMap<>();


  public static boolean preCreateNamespace(String namespace) {
    return namespaceMap.putIfAbsent(namespace, FAKE_ID) == null;
  }

  // should be used after preCreateNamespace
  public static boolean updateNamespace(String namespace, String id) {
    if (!namespaceMap.containsKey(namespace) || !FAKE_ID.equals(namespaceMap.get(namespace))) {
      return false;
    }
    namespaceMap.put(namespace, id);
    return true;
  }

}
