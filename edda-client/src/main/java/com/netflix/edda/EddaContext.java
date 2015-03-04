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

  private static final AtomicReference<RxHttp> HTTP = new AtomicReference<RxHttp>(new RxHttp(null));

  protected static void setRxHttp(RxHttp rxHttp) {
    HTTP.set(rxHttp);
  }

  protected static RxHttp getRxHttp() {
    return HTTP.get();
  }

  private RxHttp rxHttp;

  @Inject
  public EddaContext(RxHttp rxHttp) {
    this.rxHttp = rxHttp;
    setRxHttp(rxHttp);
  }

  @PostConstruct
  public void start() {
  }

  @PreDestroy
  public void stop() {
  }
}
