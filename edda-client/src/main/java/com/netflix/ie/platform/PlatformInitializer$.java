package com.netflix.ie.platform;

import com.netflix.ie.config.Configuration$;
import com.netflix.ie.ipc.Http$;
import com.netflix.ie.ipc.HttpClientWithCompression;
import com.netflix.ie.platform.NiwsHttpClient;

public class PlatformInitializer$ {
  private PlatformInitializer$() {}

  private static final Object lock = new Object();
  private static volatile boolean initialized = false;

  public static void initialize() {
    if (!initialized) {
      synchronized(lock) {
        if (!initialized) {
          Configuration$.setBackingStore(new PersistedPropertiesConfiguration());
          Http$.setClient(new HttpClientWithCompression(new NiwsHttpClient()));
          initialized = true;
        }
      }
    }
  }
}
