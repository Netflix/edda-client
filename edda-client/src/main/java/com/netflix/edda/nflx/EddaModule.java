package com.netflix.edda.nflx;

import com.google.inject.AbstractModule;

import com.netflix.config.ConfigurationManager;
import com.netflix.spectator.nflx.SpectatorModule;
import com.netflix.edda.EddaContext;

public class EddaModule extends AbstractModule {
  @Override protected void configure() {
    install(new SpectatorModule());
    bind(EddaContext.class).asEagerSingleton();

    try {
      ConfigurationManager.loadPropertiesFromResources("edda.niws.properties");
    }
    catch (java.io.IOException e) {
      throw new RuntimeException("Failed to load edda properties", e);
    }
  }
}
