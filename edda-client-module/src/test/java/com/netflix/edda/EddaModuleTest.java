/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.edda;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.netflix.iep.eureka.EurekaModule;
import com.netflix.iep.rxnetty.RxNettyModule;
import com.netflix.iep.http.RxHttp;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.netflix.spectator.nflx.SpectatorModule;

@RunWith(JUnit4.class)
public class EddaModuleTest {

  @Test
  public void module() throws java.io.IOException {
    Injector injector = Guice.createInjector(
      new EddaModule(),
      new RxNettyModule(),
      //new SpectatorModule(),
      new EurekaModule()
    );
    Assert.assertNotNull(injector.getInstance(RxHttp.class));
    Assert.assertNotNull(EddaContext.getContext().getRxHttp());
  }
}
