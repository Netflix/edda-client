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
package com.netflix.ie.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyHelper {
  private ProxyHelper() {}

  @SuppressWarnings("unchecked")
  public static <T> T wrapper(final Class<T> ctype, final T delegate, final Object overrides) {
    InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
          Method m = overrides.getClass().getMethod(method.getName(), method.getParameterTypes());
          return m.invoke(overrides, args);
        }
        catch(NoSuchMethodException e) {
          return method.invoke(delegate, args);
        }
        catch(InvocationTargetException e) {
          throw e.getCause();
        }
      }
    };

    return (T) Proxy.newProxyInstance(ctype.getClassLoader(), new Class[]{ctype}, handler);
  }

  @SuppressWarnings("unchecked")
  public static <T> T unsupported(final Class<T> ctype) {
    InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        throw new UnsupportedOperationException(ctype.getName() + "." + method.getName());
      }
    };
    return (T) Proxy.newProxyInstance(ctype.getClassLoader(), new Class[]{ctype}, handler);
  }

  @SuppressWarnings("unchecked")
  public static <T> T unsupported(final Class<T> ctype, final Object overrides) {
    InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
          Method m = overrides.getClass().getMethod(method.getName(), method.getParameterTypes());
          return m.invoke(overrides, args);
        }
        catch(NoSuchMethodException e) {
          throw new UnsupportedOperationException(ctype.getName() + "." + method.getName());
        }
        catch(InvocationTargetException e) {
          throw e.getCause();
        }
      }
    };

    return (T) Proxy.newProxyInstance(ctype.getClassLoader(), new Class[]{ctype}, handler);
  }
}
