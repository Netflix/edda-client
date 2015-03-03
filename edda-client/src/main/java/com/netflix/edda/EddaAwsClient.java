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
package com.netflix.edda;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import iep.io.reactivex.netty.protocol.http.client.HttpClientResponse;
import iep.rx.Observable;
import iep.rx.functions.Func1;
import iep.rx.functions.Func2;
import iep.rx.schedulers.Schedulers;

import com.fasterxml.jackson.core.type.TypeReference;

import com.amazonaws.*;
import com.amazonaws.handlers.RequestHandler2;
import com.amazonaws.http.HttpResponse;

import com.netflix.edda.util.ProxyHelper;

import org.apache.http.client.methods.HttpGet;

abstract public class EddaAwsClient {
  final AwsConfiguration config;
  final String vip;
  final String region;
  final RequestHandler2 requestHandler;

  public EddaAwsClient(AwsConfiguration config, String vip, String region, RequestHandler2 requestHandler) {
    this.config = config;
    this.vip = vip;
    this.region = region;
    this.requestHandler = requestHandler;
  }

  public void shutdown() {}

  protected <T> T readOnly(Class<T> c) {
    return ProxyHelper.unsupported(c, this);
  }

  protected <T> T wrapAwsClient(Class<T> c, T delegate) {
    return ProxyHelper.wrapper(c, delegate, this);
  }

  protected EddaResponse doGet(final EddaRequest<?> request) {
    EddaResponse response = EddaContext.getRxHttp().get(request.getUrl())
    .flatMap(new Func1<HttpClientResponse<ByteBuf>,Observable<EddaResponse>>() {
      @Override
      public Observable<EddaResponse> call(HttpClientResponse<ByteBuf> response) {
        Map<String, String> headers = new HashMap<>();
        for (String header : response.getHeaders().names()) {
          headers.put(header, response.getHeaders().get(header));
        }

        EddaResponse eddaResponse = new EddaResponse(response.getStatus().code(), response.getStatus().reasonPhrase(), headers);
        if (response.getStatus().code() != 200) {
          AmazonServiceException e = new AmazonServiceException("Failed to fetch " + request.getUrl());
          e.setStatusCode(response.getStatus().code());
          e.setErrorCode(request.getServiceName());
          e.setRequestId(request.getUrl());
          eddaResponse.setException(e);
          return iep.rx.Observable.just(eddaResponse);
        }

        return response.getContent()
        .reduce(
          eddaResponse,
          new Func2<EddaResponse, ByteBuf, EddaResponse>() {
            @Override
            public EddaResponse call(EddaResponse eddaResponse, ByteBuf bb) {
              try {
                bb.readBytes(eddaResponse.getOutputStream(), bb.readableBytes());
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
              return eddaResponse;
            }
          }
        );
      }
    })
    .subscribeOn(Schedulers.io())
    .toBlocking()
    .single();

    if (response.hasException()) {
      throw notifyException(request, response);
    }

    return response;
  }

  protected <T> T parse(TypeReference<T> ref, EddaResponse response) throws IOException {
    return parse(ref, response.newContentStream());
  }

  protected String mkUrl(String url) {
    return url.replaceAll("\\$\\{vip\\}", vip).replaceAll("\\$\\{region\\}", region);
  }

  protected <T> T parse(TypeReference<T> ref, byte[] body) throws IOException {
    return parse(ref, new ByteArrayInputStream(body));
  }

  protected <T> T parse(TypeReference<T> ref, InputStream content) throws IOException {
    return JsonHelper.createParser(content).readValueAs(ref);
  }

  protected void validateEmpty(String name, String s) {
    if (s != null && s.length() > 0)
      throw new UnsupportedOperationException(name + " not supported");
  }

  protected void validateNotEmpty(String name, String s) {
    if (s == null || s.length() == 0)
      throw new UnsupportedOperationException(name + " required");
  }

  protected void validateEmpty(String name, Boolean b) {
    if (b != null)
      throw new UnsupportedOperationException(name + " not supported");
  }

  protected <T> void validateEmpty(String name, List<T> list) {
    if (list != null && list.size() > 0)
      throw new UnsupportedOperationException(name + " not supported");
  }

  protected boolean shouldFilter(String s) {
    return (s != null && s.length() > 0);
  }

  protected boolean shouldFilter(List<String> list) {
    return (list != null && list.size() > 0);
  }

  protected boolean matches(String s, String v) {
    return !shouldFilter(s) || s.equals(v);
  }

  protected boolean matches(List<String> list, String v) {
    return !shouldFilter(list) || list.contains(v);
  }

  static class EddaRequest<T> extends DefaultRequest<T> {
    public EddaRequest(AmazonWebServiceRequest originalRequest, String baseUrl, String relativeUri) {
      super(originalRequest, "Edda");
      setEndpoint(URI.create(baseUrl));
      setResourcePath(relativeUri);
    }

    public String getUrl() {
      return URI.create(getEndpoint().toString() + getResourcePath()).normalize().toString();
    }
  }

  static class EddaResponse {
    private final int statusCode;
    private final String statusText;
    private final Map<String, String> headers;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private RuntimeException exception;

    public EddaResponse(int statusCode, String statusText, Map<String, String> headers) {
      this.statusCode = statusCode;
      this.statusText = statusText;
      this.headers = headers;
    }

    public void setException(RuntimeException exception) {
      this.exception = exception;
    }

    public boolean hasException() {
      return exception != null;
    }

    public RuntimeException getException() {
      return exception;
    }

    public ByteArrayOutputStream getOutputStream() {
      return outputStream;
    }

    public InputStream newContentStream() {
      return new ByteArrayInputStream(outputStream.toByteArray());
    }

    HttpResponse toHttpResponse(EddaRequest<?> request) {
      URI uri = URI.create(request.getUrl());
      HttpResponse response = new HttpResponse(request, new HttpGet(uri));
      response.setContent(newContentStream());
      response.setStatusCode(statusCode);
      response.setStatusText(statusText);
      for (Map.Entry<String, String> header : headers.entrySet()) {
        response.addHeader(header.getKey(), header.getValue());
      }
      return response;
    }

    <T> Response<T> toResponse(EddaRequest<?> request, T awsResponse) {
      return new Response<>(awsResponse, toHttpResponse(request));
    }
  }

  protected RuntimeException notifyException(EddaRequest<?> request, EddaResponse eddaResponse) {
    if (requestHandler != null) {
      requestHandler.afterError(request, eddaResponse.toResponse(request, null), eddaResponse.getException());
    }

    return eddaResponse.getException();
  }

  protected <T extends AmazonWebServiceRequest> EddaRequest<T> buildRequest(T originalRequest, String relativeUri) {
    EddaRequest<T> req = new EddaRequest<>(originalRequest, mkUrl(config.url()), relativeUri);
    if (requestHandler != null) {
      requestHandler.beforeRequest(req);
    }
    return req;
  }

  protected <T> T handleResponse(EddaResponse resp, EddaRequest<?> request, T awsResp) {
    Response<T> response = resp.toResponse(request, awsResp);
    if (requestHandler != null) {
      requestHandler.afterResponse(request, response);
    }

    return response.getAwsResponse();
  }

}
