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

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;

import com.netflix.spectator.nflx.RxHttp;

import com.netflix.ie.ipc.AbstractHttpClient;
import com.netflix.ie.ipc.HttpConf;
import com.netflix.ie.ipc.HttpException;
import com.netflix.ie.ipc.HttpResponse;

public class NiwsHttpClient extends AbstractHttpClient {
  private static Logger LOGGER = LoggerFactory.getLogger(NiwsHttpClient.class);

  private static Pattern NIWS_URI = Pattern.compile("niws://([^/]+)(.*)");

  @Override
  protected HttpResponse execute(
    String method,
    URI uri,
    byte[] body,
    HttpConf conf
  ) {
    if (method.toLowerCase().equals("get")) {
      return mkHttpResponse(uri, RxHttp.get(uri));
    }
    else {
      throw new UnsupportedOperationException(method + " method not supported");
    }
  }

  private HttpResponse mkHttpResponse(
    final URI uri,
    Observable<HttpClientResponse<ByteBuf>> response
  ) {
    return response.flatMap(new Func1<HttpClientResponse<ByteBuf>,Observable<HttpResponse>>() {
      @Override
      public Observable<HttpResponse> call(final HttpClientResponse<ByteBuf> r) {
        return r.getContent().map(new Func1<ByteBuf,HttpResponse>() {
          @Override
          public HttpResponse call(ByteBuf b) {
            try {
              HttpResponseHeaders headers = r.getHeaders();
              Map<String,String> headerMap = new HashMap<String,String>();
              for (String name : headers.names()) {
                 headerMap.put(name, Joiner.on(",").join(headers.getAll(name)));
              }
              java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream(b.capacity());
              b.readBytes(bout, b.capacity());
              bout.close();
              return new HttpResponse(
                uri,
                r.getStatus().code(),
                headerMap,
                bout.toByteArray()
              );
            }
            catch (Exception e) {
              return new HttpResponse(uri, 400, e);
            }
          }
        });
      }
    }).toBlocking().single();
  }
}
