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

public class HttpException extends Exception {
  private int status;
  private String msg;

  public HttpException(int status, String msg) {
    this(status, msg, null);
  }

  public HttpException(int status, String msg, Exception e) {
    super("status: " + status + ", message: " + msg, e);
    this.status = status;
    this.msg = msg;
  }

  public int status() {
    return status;
  }

  public String msg() {
    return msg;
  }
}
