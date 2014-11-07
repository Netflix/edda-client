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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HttpResponse {
  private final String uri;
  private final int status;
  private final Map<String,String> headers;
  private final byte[] body;
  private final Throwable error;

  // lazy val
  private volatile int bitmap$0;
  private String bodyString;

  public HttpResponse(URI uri, int status, Map<String,String> headers, byte[] body) {
    this(uri.toString(), status, headers, body);
  }

  public HttpResponse(String uri, int status, Map<String,String> headers, byte[] body) {
    this.uri = uri;
    this.status = status;
    this.headers = Collections.unmodifiableMap(headers);
    this.body = Arrays.copyOf(body, body.length);
    this.error = null;
  }

  public HttpResponse(URI uri, int status, Throwable e) {
    this(uri.toString(), status, e);
  }

  public HttpResponse(String uri, int status, Throwable e) {
    this.uri = uri;
    this.status = status;
    this.headers = Collections.emptyMap();
    this.body = ((e.getMessage() == null)?e.getClass().getSimpleName():e.getMessage()).getBytes();
    this.error = e;
  }

  public String uri() {
    return uri;
  }

  public int status() {
    return status;
  }

  public Map<String,String> headers() {
    return headers;
  }

  public byte[] body() {
    return Arrays.copyOf(body, body.length);
  }

  public Throwable error() {
    return error;
  }

  public String bodyString() throws UnsupportedEncodingException {
    if ((bitmap$0 & 1) == 0) {
      synchronized(this) {
        if ((bitmap$0 & 1) == 0) {
          bodyString = new String(body, "UTF-8");
          bitmap$0 = bitmap$0 | 1;
        }
      }
    }
    return bodyString;
  }

  public HttpResponse throwUnless(int expectedStatus) throws HttpException {
    if (status != expectedStatus) {
      String str = "unparseable response body";
      try { str = bodyString(); }
      catch(UnsupportedEncodingException e) {}
      String[] lines = str.split("\n");
      String msg = "empty response body";
      for (int i = 0; i < lines.length; i++) {
        String line = lines[i].trim();
        if (!line.equals("")) {
          msg = line;
          break;
        }
      }
      throw new HttpException(status, msg, error);
    }
    return this;
  }

  @Override
  public String toString() {
    try {
      return "HttpResponse(" + uri + ", " + status + ", " + headers.toString() + ", " + bodyString() + ")";
    }
    catch (UnsupportedEncodingException e) {
      return "HttpResponse(" + uri + ", " + status + ", " + headers.toString() + ", unparseable body)";
    }
  }
}
