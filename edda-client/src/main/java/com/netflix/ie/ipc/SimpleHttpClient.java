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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import com.google.common.base.Joiner;

import com.netflix.ie.util.Hash$;

public class SimpleHttpClient extends HttpClient {
  private final static Logger LOGGER = LoggerFactory.getLogger(SimpleHttpClient.class);

  @Override
  protected HttpResponse execute(
    String method,
    URI uri,
    byte[] body,
    HttpConf conf
  ) {
    try {
      if (uri.getScheme().equals("http")) return httpExecute(method, uri, body, conf);
      else if (uri.getScheme().equals("https")) return httpExecute(method, uri, body, conf);
      else if (uri.getScheme().equals("file")) return fileExecute(method, uri, body);
      throw new IllegalArgumentException("unsupported scheme [" + uri.getScheme() + "]");
    }
    catch (Exception e) {
      return new HttpResponse(uri, 400, e);
    }
  }

  private HttpResponse fileExecute(
    String method,
    URI rawUri,
    byte[] body
  ) throws Exception {
    URI uri = rawUri;
    try {
      String suffix = (body == null) ? "" : ";" + method + "=" + Hash$.md5(body);
      uri = new URI(rawUri.toString().replace("?", ";") + suffix);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(method + " " + uri);
      }
      URL url = uri.toURL();
      URLConnection c = url.openConnection();
      InputStream in = c.getInputStream();
      byte[] resBody = null;
      try { resBody = IOUtils.toByteArray(in); }
      finally { in.close(); }
      return new HttpResponse(uri, 200, new HashMap<String,String>(), resBody);
    }
    catch(Exception e) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn("request failed: " + method + " " + rawUri, e);
      }
      throw e;
    }
  }

  private HttpResponse httpExecute(
    String method,
    URI uri,
    byte[] body,
    HttpConf conf
  ) throws Exception {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(method + " " + uri);
    }
    URL url = uri.toURL();
    HttpURLConnection c = (HttpURLConnection) url.openConnection();
    try {
      c.setRequestMethod(method);
      c.setConnectTimeout(conf.connectTimeout());
      c.setReadTimeout(conf.readTimeout());
      for (Map.Entry<String,String> e : conf.headers().entrySet()) {
        c.setRequestProperty(e.getKey(), e.getValue());
      }
      c.setDoInput(true);

      if (body != null) {
        c.setDoOutput(true);
        OutputStream out = c.getOutputStream();
        try { out.write(body); }
        finally { out.close(); }
      }

      c.connect();
      int status = c.getResponseCode();
      Map<String,String> headers = new HashMap<String,String>();
      for (Map.Entry<String,List<String>> e : c.getHeaderFields().entrySet()) {
        headers.put(e.getKey(), Joiner.on(",").join(e.getValue()));
      }
      InputStream in = (status >= 400) ? c.getErrorStream() : c.getInputStream();
      byte[] resBody = null;
      try { resBody = IOUtils.toByteArray(in); }
      finally { in.close(); }
      return new HttpResponse(uri, status, headers, resBody);
    }
    catch(Exception e) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn("request failed: " + method + " " + uri, e);
      }
      throw e;
    }
    finally {
      c.disconnect();
    }
  }
}
