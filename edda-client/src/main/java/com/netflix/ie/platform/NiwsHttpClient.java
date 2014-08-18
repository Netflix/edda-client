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

import com.netflix.client.ClientException;
import com.netflix.client.http.HttpRequest;
import com.netflix.niws.client.http.RestClient;
import com.netflix.spectator.ribbon.RestClientFactory;

import com.netflix.ie.ipc.HttpClient;
import com.netflix.ie.ipc.HttpConf;
import com.netflix.ie.ipc.HttpException;
import com.netflix.ie.ipc.HttpResponse;

public class NiwsHttpClient extends HttpClient {
  private static Logger LOGGER = LoggerFactory.getLogger(NiwsHttpClient.class);

  private static Pattern NIWS_URI = Pattern.compile("niws://([^/]+)(.*)");

  @Override
  protected HttpResponse execute(
    String method,
    URI uri,
    byte[] body,
    HttpConf conf
  ) {
    URI httpUri = uri;
    com.netflix.client.http.HttpResponse response = null;
    try {
      if (uri.getScheme().equals("niws"))
        httpUri = new URI(fixPath(uri.getRawPath()) + fix("?", uri.getRawQuery()));
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(method + " " + uri + " => " + httpUri);
      HttpRequest.Builder builder = new HttpRequest.Builder()
        .verb(HttpRequest.Verb.valueOf(method))
        .uri(httpUri)
        .entity(body);
      for (Map.Entry<String,String> e : conf.headers().entrySet())
        builder.header(e.getKey(), e.getValue());
      addTraceHeaders(builder);

      RestClient httpClient = getClient(uri);

      response = (httpUri.isAbsolute())
        ? httpClient.execute(builder.build())
        : httpClient.executeWithLoadBalancer(builder.build());
    }
    catch (ClientException e) {
      return new HttpResponse(httpUri, e.getErrorCode(), e);
    }
    catch (Exception e) {
      return new HttpResponse(httpUri, 400, e);
    }

    try {
      int status = response.getStatus();
      Map<String,String> headers = new HashMap<String,String>();
      for (Map.Entry<String,Collection<String>> e : response.getHeaders().entrySet()) {
        headers.put(e.getKey(), Joiner.on(",").join(e.getValue()));
      }
      byte[] resBody = IOUtils.toByteArray(response.getInputStream());
      return new HttpResponse(httpUri, status, headers, resBody);
    }
    catch (Exception e) {
      return new HttpResponse(httpUri, 500, e);
    }
    finally {
      response.close();
    }
  }

  private String getHost(URI uri) {
    Matcher m = NIWS_URI.matcher(uri.toString());
    if (m.matches()) return m.group(1);
    return uri.getHost();
  }

  private RestClient getClient(URI uri) {
    Matcher m = NIWS_URI.matcher(uri.toString());
    if (m.matches()) return RestClientFactory.getClient(m.group(1));
    return RestClientFactory.getClient("default");
  }

  private String fixPath(String path) {
    return (path.startsWith("/http://") || path.startsWith("/https://")) ? path.substring(1) : path;
  }

  private String fix(String sep, String str) {
    if (str == null) return "";
    return sep + str;
  }

  private void addTraceHeaders(HttpRequest.Builder builder) {
    builder.header("X-Netflix.environment", NetflixEnvironment$.env());
    builder.header("X-Netflix.client.requestStartTime", String.valueOf(System.currentTimeMillis()));
    builder.header("X-Netflix.client.appid", NetflixEnvironment$.app());
    builder.header("X-Netflix.client.asg.name", NetflixEnvironment$.asg());
    builder.header("X-Netflix.client.az", NetflixEnvironment$.zone());
    builder.header("X-Netflix.client.instid", NetflixEnvironment$.instanceId());
  }
}
