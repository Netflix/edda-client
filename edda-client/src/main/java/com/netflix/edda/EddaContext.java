package com.netflix.edda;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.iep.http.RxHttp;

@Singleton
public class EddaContext {
  private static final Logger LOGGER = LoggerFactory.getLogger(EddaContext.class);

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
  public void start() {
    CONTEXT.set(this);
  }

  @PreDestroy
  public void stop() {
    CONTEXT.set(null);
  }

  public RxHttp getRxHttp() {
    return rxHttp;
  }
}
