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
import java.net.URISyntaxException;

public abstract class AbstractHttpClient implements HttpClient {
  @Override
  public HttpResponse get(String uri) {
    return get(uri, new HttpConf());
  }

  @Override
  public HttpResponse get(String uri, HttpConf conf) {
    try {
      return get(new URI(uri), conf);
    }
    catch (URISyntaxException e) {
      return new HttpResponse(uri, 400, e);
    }
  }

  @Override
  public HttpResponse get(URI uri, HttpConf conf) {
    return execute("GET", uri, null, conf);
  }

  abstract protected HttpResponse execute(
    String method,
    URI uri,
    byte[] body,
    HttpConf conf
  );
}
