package com.netflix.edda;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.ConfigurationManager;

import iep.com.netflix.iep.http.RxHttp;

@Singleton
public class EddaContext {
  private static final Logger LOGGER = LoggerFactory.getLogger(EddaContext.class);
  private static final String ENABLED_PROP = "edda-client.nflx.enabled";
  private static final String CONFIG_FILE = "edda-client.properties";

  private static final AtomicReference<EddaContext> CONTEXT = new AtomicReference<EddaContext>(null);

  protected static EddaContext getContext() {
    EddaContext ctx = CONTEXT.get();
    if (ctx == null) throw new IllegalStateException("EddaContext not initialized");
    return ctx;
  }

  private final RxHttp rxHttp;

  @Inject
  public EddaContext(RxHttp rxHttp) {
    this.rxHttp = rxHttp;
  }

  @PostConstruct
  public void init() throws IOException {
    CONTEXT.set(this);
    if (ConfigurationManager.getConfigInstance().getBoolean(ENABLED_PROP, true)) {
      LOGGER.debug("loading properties: " + CONFIG_FILE);
      ConfigurationManager.loadPropertiesFromResources(CONFIG_FILE);
    }
    else {
      LOGGER.debug("context not enabled, set " + ENABLED_PROP + "=true to enable");
    }
  }

  @PreDestroy
  public void destroy() {
    CONTEXT.set(null);
  }

  public RxHttp getRxHttp() {
    return rxHttp;
  }
}
