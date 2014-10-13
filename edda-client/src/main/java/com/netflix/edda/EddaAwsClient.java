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

  protected <T> T readOnly(Class<T> c) {
    return ProxyHelper.unsupported(c, this);
  }

  protected <T> T wrapAwsClient(Class<T> c, T delegate) {
    return ProxyHelper.wrapper(c, delegate, this);
  }

  HttpResponse doGet(String uri) {
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

  <T> T parse(TypeReference<T> ref, byte[] body) throws IOException {
      return JsonHelper.createParser(new ByteArrayInputStream(body)).readValueAs(ref);
  }
}
