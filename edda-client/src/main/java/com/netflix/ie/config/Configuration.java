/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.ie.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.netflix.ie.util.Strings;

@Singleton
public final class Configuration {
  private final static Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

  @Inject
  private IConfiguration iConfiguration;

  private final AtomicReference<IConfiguration> backingStoreRef =
    new AtomicReference<IConfiguration>(null);

  private Configuration() {
    if (iConfiguration == null) {
      LOGGER.debug("iConfiguration not defined - using SystemPropertyConfiguration");
      backingStoreRef.set(new SystemPropertyConfiguration("netflix"));
    }
    else
      backingStoreRef.set(iConfiguration);
  }

  private static class SingletonHolder {
    private static final Configuration INSTANCE = new Configuration();
  }


  public static void setBackingStore(IConfiguration c) {
    SingletonHolder.INSTANCE.backingStoreRef.set(c);
  }

  public static <T> T apply(Class<T> ctype) {
    String pkg = ctype.getPackage().getName();
    String prefix = (pkg.startsWith("com.")) ? pkg.substring("com.".length()) : pkg;
    return newProxy(ctype, prefix);
  }

  public static <T> T newProxy(Class<T> ctype, String prefix) {
    return (T) newProxyImpl(ctype, prefix, SingletonHolder.INSTANCE.backingStoreRef.get());
  }

  @SuppressWarnings("unchecked")
  public static <T> T newProxyImpl(
    final Class<T> ctype,
    final String prefix,
    final IConfiguration backingStore
   ) {
    InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("get")) {
          return backingStore.get((args[0] == null) ? null : args[0].toString());
        }
        else {
          Class rt = method.getReturnType();
          String key = (prefix == null) ? method.getName() : prefix + "." + method.getName();
          if (IConfiguration.class.isAssignableFrom(rt)) {
            return newProxyImpl(rt, key, backingStore);
          }
          else {
            String value = backingStore.get(key);
            if (value == null) {
              DefaultValue anno = method.getAnnotation(DefaultValue.class);
              value = (anno == null) ? null : anno.value();
            }
            if (value == null) {
              if (rt.isPrimitive())
                throw new IllegalStateException("no value for property " + method.getName());
               return null;
            }
            return Strings.cast(rt, value);
          }
        }
      }
    };
    return (T) Proxy.newProxyInstance(ctype.getClassLoader(), new Class[]{ctype}, handler);
  }
}
