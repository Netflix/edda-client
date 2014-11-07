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
package com.netflix.ie.platform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import com.google.common.base.Joiner;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;

import com.netflix.spectator.nflx.RxHttp;

import com.netflix.ie.ipc.AbstractHttpClient;
import com.netflix.ie.ipc.HttpConf;
import com.netflix.ie.ipc.HttpException;
import com.netflix.ie.ipc.HttpResponse;
import com.netflix.ie.ipc.HttpClient;
import com.netflix.ie.ipc.SimpleHttpClient;

public class NiwsHttpClient extends AbstractHttpClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(NiwsHttpClient.class);

  private static final Pattern NIWS_URI = Pattern.compile("niws://([^/]+)(.*)");

  private static final HttpClient SIMPLE_HTTP_CLIENT = new SimpleHttpClient();

  @Override
  public HttpResponse execute(
    String method,
    URI uri,
    byte[] body,
    HttpConf conf
  ) {
    if ("file".equals(uri.getScheme().toLowerCase()))
      return SIMPLE_HTTP_CLIENT.execute(method, uri, body, conf);

    if ("get".equals(method.toLowerCase()))
      return mkHttpResponse(uri, RxHttp.get(uri));

    throw new UnsupportedOperationException("http request not supported: [" + method + "] " + uri);
  }

  private HttpResponse mkHttpResponse(
    final URI uri,
    Observable<HttpClientResponse<ByteBuf>> response
  ) {
    return response.flatMap(new Func1<HttpClientResponse<ByteBuf>,Observable<HttpResponse>>() {
      @Override
      public Observable<HttpResponse> call(final HttpClientResponse<ByteBuf> r) {
        return r.getContent()
          .reduce(new ByteArrayOutputStream(), new Func2<ByteArrayOutputStream,ByteBuf,ByteArrayOutputStream>() {
            @Override
            public ByteArrayOutputStream call(ByteArrayOutputStream out, ByteBuf b) {
              try {
                b.readBytes(out, b.capacity());
              }
              catch(IOException e) {
                Observable.error(e);
              }
              return out;
            }
          })
          .map(new Func1<ByteArrayOutputStream,HttpResponse>() {
            @Override
            public HttpResponse call(ByteArrayOutputStream bout) {
              HttpResponseHeaders headers = r.getHeaders();
              Map<String,String> headerMap = new HashMap<String,String>();
              for (String name : headers.names()) {
                 headerMap.put(name, Joiner.on(",").join(headers.getAll(name)));
              }
              return new HttpResponse(
                uri,
                r.getStatus().code(),
                headerMap,
                bout.toByteArray()
              );
            }
          })
          .onErrorReturn(new Func1<Throwable,HttpResponse>() {
            @Override
            public HttpResponse call(Throwable t) {
              return new HttpResponse(uri, 400, t);
            }
          });
      }
    }).toBlocking().single();
  }
}
