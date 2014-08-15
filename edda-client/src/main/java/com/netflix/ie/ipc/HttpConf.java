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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpConf {
  private int connectTimeout = 5000;
  private int readTimeout = 60000;
  private Map<String,String> headers = Collections.emptyMap();

  public int connectTimeout() {
    return connectTimeout;
  }
  public int readTimeout() {
    return readTimeout;
  }

  public Map<String,String> headers() {
    return headers;
  }

  public HttpConf withConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public HttpConf withReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }

  public HttpConf withHeaders(Map<String,String> headers) {
    this.headers = Collections.unmodifiableMap(headers);
    return this;
  }

  public HttpConf withHeader(String key, String value) {
    Map<String,String> headers = new HashMap<String,String>(this.headers);
    headers.put(key, value);
    return withHeaders(headers);
  }
}
