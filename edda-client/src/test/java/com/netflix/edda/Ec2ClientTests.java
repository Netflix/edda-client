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

import java.util.Map;
import java.util.HashMap;

import static org.junit.Assert.*;
import org.junit.Test;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;

import com.netflix.ie.config.Configuration$;
import com.netflix.ie.config.MapConfiguration;

public class Ec2ClientTests {

  private AwsConfiguration mkConfig() {
    String userDir = System.getProperty("user.dir");
    final String resourceDir = "file://" + userDir + "/src/test/resources";

    Map<String,String> props = new HashMap<String,String>() {{
      put("url", resourceDir + "/edda");
    }};
    return Configuration$.newProxyImpl(
      AwsConfiguration.class,
      null,
      new MapConfiguration(null, props)
    );
  }

  @Test
  public void describeSubnets() {
    AmazonEC2 client = EddaEc2Client$.readOnly(mkConfig());
    DescribeSubnetsResult res = client.describeSubnets();
    assertEquals("size", res.getSubnets().size(), 8);
  }
}
