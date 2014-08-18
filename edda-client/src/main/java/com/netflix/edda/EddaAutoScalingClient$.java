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

import com.amazonaws.services.autoscaling.AmazonAutoScaling;

import com.netflix.ie.util.ProxyHelper$;

public class EddaAutoScalingClient$ {
  private EddaAutoScalingClient$() {}

  public static AmazonAutoScaling readOnly(AwsConfiguration config) {
    return ProxyHelper$.unsupported(AmazonAutoScaling.class, new EddaAutoScalingClient(config));
  }

  public static AmazonAutoScaling wrap(AmazonAutoScaling delegate, AwsConfiguration config) {
    return ProxyHelper$.wrapper(AmazonAutoScaling.class, delegate, new EddaAutoScalingClient(config));
  }
}
