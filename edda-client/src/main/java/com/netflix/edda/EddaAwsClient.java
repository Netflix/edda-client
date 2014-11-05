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
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import com.amazonaws.AmazonServiceException;

import com.netflix.ie.ipc.Http;
import com.netflix.ie.ipc.HttpResponse;
import com.netflix.ie.util.ProxyHelper;
import com.netflix.ie.platform.PlatformInitializer;

abstract public class EddaAwsClient {
  final AwsConfiguration config;

  public EddaAwsClient(AwsConfiguration config) {
    this.config = config;
    PlatformInitializer.loadResource("edda.niws.properties");
  }

  public void shutdown() {}

  protected <T> T readOnly(Class<T> c) {
    return ProxyHelper.unsupported(c, this);
  }

  protected <T> T wrapAwsClient(Class<T> c, T delegate) {
    return ProxyHelper.wrapper(c, delegate, this);
  }

  protected HttpResponse doGet(String uri) {
    HttpResponse res = Http.get(uri);
    if (res.status() != 200) {
      AmazonServiceException e = new AmazonServiceException("Failed to fetch " + uri);
      e.setStatusCode(res.status());
      e.setErrorCode("Edda");
      e.setRequestId(uri);
      throw e;
    }
    return res;
  }

  protected <T> T parse(TypeReference<T> ref, byte[] body) throws IOException {
      return JsonHelper.createParser(new ByteArrayInputStream(body)).readValueAs(ref);
  }

  protected void validateEmpty(String name, String s) {
    if (s != null && s.length() > 0)
      throw new UnsupportedOperationException(name + " not supported");
  }

  protected void validateNotEmpty(String name, String s) {
    if (s == null || s.length() == 0)
      throw new UnsupportedOperationException(name + " required");
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
