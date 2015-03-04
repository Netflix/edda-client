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

import com.netflix.iep.config.IConfiguration;
import com.netflix.iep.config.DefaultValue;

import org.joda.time.Duration;

public interface AwsConfiguration extends IConfiguration {
  /** Should we mock dependencies? */
  @DefaultValue("false")
  public boolean useMock();

  /** Should we wrap a real AWS client capable of accessing AWS apis directly */
  @DefaultValue("false")
  public boolean wrapAwsClient();

  /**
   * Should we attempt to use edda for reads? This is only supported for the EC2 api and will be
   * ignored for all others.
   */
  @DefaultValue("true")
  public boolean useEdda();

  @DefaultValue("vip://edda-client:${vip}")
  public String url();

  /////////////////////////////////////////////////////////////////////////////
  // Settings below are used to setup amazon ClientConfiguration object

  @DefaultValue("PT10S")
  public Duration connectionTimeout();

  @DefaultValue("200")
  public int maxConnections();

  @DefaultValue("2")
  public int maxErrorRetry();

  @DefaultValue("PT60S")
  public Duration socketTimeout();
}
