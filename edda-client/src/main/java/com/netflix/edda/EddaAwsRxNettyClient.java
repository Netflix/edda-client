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

import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import iep.io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import iep.com.netflix.iep.http.ByteBufs;

import com.fasterxml.jackson.core.type.TypeReference;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ServiceResult;

import com.netflix.edda.util.ProxyHelper;

abstract public class EddaAwsRxNettyClient {
  final AwsConfiguration config;
  final String vip;
  final String region;

  public EddaAwsRxNettyClient(AwsConfiguration config, String vip, String region) {
    this.config = config;
    this.vip = vip;
    this.region = region;
  }

  public void shutdown() {}

  protected <T> T readOnly(Class<T> c) {
    return ProxyHelper.unsupported(c, this);
  }

  protected <T> T wrapAwsClient(Class<T> c, T delegate) {
    return ProxyHelper.wrapper(c, delegate, this);
  }

  protected <T> Observable<ServiceResult<T>> doGet(
    final TypeReference<T> ref,
    final String uri
  ) {
    return EddaContext.getContext().getRxHttp().get(mkUrl(uri))
    .flatMap(response -> {
      if (response.getStatus().code() != 200) {
        AmazonServiceException e = new AmazonServiceException("Failed to fetch " + uri);
        e.setStatusCode(response.getStatus().code());
        e.setErrorCode("Edda");
        e.setRequestId(uri);
        return rx.Observable.error(e);
      }
      return response.getContent()
      .compose(ByteBufs.json()).map(byteBuf -> {
        InputStream is = new ByteBufInputStream(byteBuf);
        try {
          return (T) JsonHelper.createParser(is).readValueAs(ref);
        }
        catch (Exception e) {
          throw new RuntimeException("failed to get url: " + uri, e);
        }
        //finally {
          //if (is != null) try { is.close(); } catch (IOException e) {}
        //}
      })
      .map(t -> {
        return new ServiceResult<T>(0, t);
      });
    });
  }

  protected <T> Observable<ServiceResult<List<T>>> doGetList(
    final TypeReference<T> ref,
    final String uri
  ) {
    return EddaContext.getContext().getRxHttp().get(mkUrl(uri))
    .flatMap(response -> {
      if (response.getStatus().code() != 200) {
        AmazonServiceException e = new AmazonServiceException("Failed to fetch " + uri);
        e.setStatusCode(response.getStatus().code());
        e.setErrorCode("Edda");
        e.setRequestId(uri);
        return rx.Observable.error(e);
      }
      return response.getContent()
      .compose(ByteBufs.json()).map(byteBuf -> {
        try {
          //ByteBufInputStream is = new ByteBufInputStream(byteBuf);
          java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
          byteBuf.readBytes(out, byteBuf.readableBytes());
System.err.println(new String(out.toByteArray()));
          InputStream is = new java.io.ByteArrayInputStream(out.toByteArray());
          return (T) JsonHelper.createParser(is).readValueAs(ref);
        }
        catch (Exception e) {
          throw new RuntimeException("failed to get url: " + uri, e);
        }
        //finally {
          //if (is != null) try { is.close(); } catch (IOException e) {}
        //}
      })
      .toList()
      .map(ts -> {
        return new ServiceResult<List<T>>(0, ts);
      });
    });
  }

  protected String mkUrl(String url) {
    return url.replaceAll("\\$\\{vip\\}", vip).replaceAll("\\$\\{region\\}", region);
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
}
