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

public class PropertyFileLoader {
  private PropertyFileLoader() {}

  private static Logger LOGGER = LoggerFactory.getLogger(PropertyFileLoader.class);

  private static final Object lock = new Object();
  private static final List<String> resources = new ArrayList<String>();

  public static void loadResource(String resource) {
    if (!resources.contains(resource)) {
      synchronized(lock) {
        loadResourceImpl(resource);
      }
    }
  }

  private static void loadResourceImpl(String resource) {
    if (resources.contains(resource)) return;

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
      resources.add(resource);
    }
    catch (IOException e) {
      LOGGER.error("Failed to load config resource: " + resource, e);
    }
  }
}
