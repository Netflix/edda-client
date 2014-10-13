package com.netflix.ie.platform;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

import org.apache.commons.configuration.AbstractConfiguration;

import com.netflix.config.ConfigurationManager;

import com.netflix.ie.config.Configuration;
import com.netflix.ie.ipc.Http;
import com.netflix.ie.ipc.HttpClientWithCompression;
import com.netflix.ie.platform.NiwsHttpClient;

public class PlatformInitializer {
  private PlatformInitializer() {}

  private static Logger LOGGER = LoggerFactory.getLogger(PlatformInitializer.class);

  private static final Object lock = new Object();
  private static final List<String> resources = new ArrayList<String>();
  private static volatile boolean initialized = false;

  public static void initialize() {
    if (!initialized) {
      synchronized(lock) {
        if (!initialized) {
          Configuration.setBackingStore(new PersistedPropertiesConfiguration());
          Http.setClient(new HttpClientWithCompression(new NiwsHttpClient()));
          initialized = true;

          if (resources.size() > 0) {
            LOGGER.warn("Initialization occurring after resources were needed");
            for (String resource : resources)
              loadResourceImpl(resource);
          }
        }
      }
    }
  }

  public static void loadResource(String resource) {
    if (!initialized) return;
    if (!resources.contains(resource)) {
      synchronized(lock) {
        if (!resources.contains(resource)) {
          loadResourceImpl(resource);
          resources.add(resource);
        }
      }
    }
  }

  private static void loadResourceImpl(String resource) {
    LOGGER.info("Loading resource: " + resource);
    try {
      Properties props = new Properties();
      URL url = Resources.getResource(resource);
      props.load(new ByteArrayInputStream(Resources.toByteArray(url)));
      AbstractConfiguration config = ConfigurationManager.getConfigInstance();
      for (Map.Entry<Object,Object> e : props.entrySet()) {
        String key = e.getKey().toString();
        if (config.getProperty(key) == null)
          config.setProperty(key, e.getValue());
      }
    }
    catch (IOException e) {
      LOGGER.error("Failed to load config resource: " + resource, e);
    }
  }
}
