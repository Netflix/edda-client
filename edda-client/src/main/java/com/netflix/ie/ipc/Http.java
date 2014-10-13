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
package com.netflix.ie.ipc;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public class Http {
  private Http() {}

  private static final AtomicReference<HttpClient> clientRef =
    new AtomicReference<HttpClient>(new SimpleHttpClient());

  public static HttpClient getClient() { return clientRef.get(); }
  public static void setClient(HttpClient c) { clientRef.set(c); }

  public static HttpResponse get(String uri) {
    return clientRef.get().get(uri);
  }

  public static HttpResponse get(String uri, HttpConf conf) {
    return clientRef.get().get(uri, conf);
  }

  public static HttpResponse get(URI uri, HttpConf conf) {
    return clientRef.get().get(uri, conf);
  }
}
