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

import com.amazonaws.services.ec2.AmazonEC2;

import com.netflix.ie.util.ProxyHelper$;

public class EddaEc2Client$ {
  private EddaEc2Client$() {}

  public static AmazonEC2 readOnly(AwsConfiguration config) {
    return ProxyHelper$.unsupported(AmazonEC2.class, new EddaEc2Client(config));
  }

  public static AmazonEC2 wrap(AmazonEC2 delegate, AwsConfiguration config) {
    return ProxyHelper$.wrapper(AmazonEC2.class, delegate, new EddaEc2Client(config));
  }
}
