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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

public class HttpClientWithCompression extends HttpClient {
  private final HttpClient client;

  public HttpClientWithCompression(HttpClient client) {
    this.client = client;
  }

  private HttpConf withAcceptHeader(HttpConf conf) {
    return conf.withHeader("Accept-Encoding", "gzip");
  }

  private HttpResponse withDeflation(HttpResponse res) {
    String encoding = res.headers().get("Content-Encoding");
    if (encoding != null && encoding.contains("gzip")) {
      Map<String,String> headers = new HashMap<String,String>(res.headers());
      headers.remove("Content-Encoding");
      GZIPInputStream input = null;
      try {
        input = new GZIPInputStream(new ByteArrayInputStream(res.body()));
        return new HttpResponse(
          res.uri(),
          res.status(),
          headers,
          IOUtils.toByteArray(input)
        );
      }
      catch (IOException e) {
        return new HttpResponse(
          res.uri(),
          500,
          e
        );
      }
      finally {
        if (input != null) try { input.close(); } catch (IOException e) {}
      }
    }
    return res;
  }

  @Override
  protected HttpResponse execute(
    String method,
    URI uri,
    byte[] body,
    HttpConf conf
  ) {
    return withDeflation(client.execute(method, uri, body, withAcceptHeader(conf)));
  }
}
