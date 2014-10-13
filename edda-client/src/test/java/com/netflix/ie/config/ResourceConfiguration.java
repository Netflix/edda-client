package com.netflix.ie.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import com.google.common.io.Resources;
import com.google.common.base.Charsets;

import com.netflix.ie.config.Configuration;

public class ResourceConfiguration {
  private ResourceConfiguration() {}

  public static void load(
    String propFile
  ) throws IOException {
    load(propFile, new HashMap<String,String>());
  }

  public static void load(
    String propFile,
    Map<String,String> subs
  ) throws IOException {
    load(propFile, subs, new HashMap<String,String>());
  }

  public static void load(
    String propFile,
    Map<String,String> subs,
    Map<String,String> overrides
  ) throws IOException {
    URL propUrl = Resources.getResource(propFile);
    String propData = Resources.toString(propUrl, Charsets.UTF_8);
    for (Map.Entry e : subs.entrySet()) {
      propData = propData.replaceAll("\\{" + e.getKey() + "\\}", (String) e.getValue());
    }
    final Properties props = new Properties();
    props.load(new ByteArrayInputStream(propData.getBytes()));
    for (Map.Entry e : overrides.entrySet()) {
      props.setProperty((String) e.getKey(), (String) e.getValue());
    }
    Configuration.setBackingStore(
      new IConfiguration() {
        @Override
        public String get(String key) {
          return (String) props.getProperty(key);
        }
      }
    );
  }
}
