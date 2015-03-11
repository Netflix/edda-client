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
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import iep.io.reactivex.netty.RxNetty;
import iep.io.reactivex.netty.protocol.http.server.HttpServer;
import iep.io.reactivex.netty.protocol.http.server.file.ClassPathFileRequestHandler;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;

import com.netflix.iep.config.Configuration;
import com.netflix.iep.config.TestResourceConfiguration;
import iep.com.netflix.iep.http.RxHttp;

public class Ec2ClientTests {
  private static HttpServer<ByteBuf, ByteBuf> server;

  private static EddaContext eddaContext = new EddaContext(new RxHttp(null));

  @BeforeClass
  public static void setUp() throws Exception {
    server = RxNetty.createHttpServer(0, new ClassPathFileRequestHandler(".")).start();

    final String userDir = System.getProperty("user.dir");
    Map<String,String> subs = new HashMap<String,String>() {{
      put("user.dir", userDir);
      put("resources.url", "http://localhost:" + server.getServerPort());
    }};
    TestResourceConfiguration.load("edda.test.properties", subs);
    eddaContext.start();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    eddaContext.stop();
  }

  @Test
  public void describeSubnets() {
    AmazonEC2 client = AwsClientFactory.newEc2Client();
    DescribeSubnetsResult res = client.describeSubnets();
    assertEquals("size", res.getSubnets().size(), 8);

    String id = "subnet-30ef1559";
    res = client.describeSubnets(new DescribeSubnetsRequest().withSubnetIds(id));
    assertEquals("size", res.getSubnets().size(), 1);
    assertEquals("id", res.getSubnets().get(0).getSubnetId(), id);

    String id2 = "subnet-0962c560";
    res = client.describeSubnets(new DescribeSubnetsRequest().withSubnetIds(id, id2));
    assertEquals("size", res.getSubnets().size(), 2);
    assertEquals("id1", res.getSubnets().get(0).getSubnetId(), id);
    assertEquals("id2", res.getSubnets().get(1).getSubnetId(), id2);
  }
}
