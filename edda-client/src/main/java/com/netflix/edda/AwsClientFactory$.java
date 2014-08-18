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

import com.amazonaws.ResponseMetadata;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;

import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;

import com.netflix.ie.config.Configuration$;
import com.netflix.ie.platform.NetflixEnvironment$;

public class AwsClientFactory$ {
  private AwsClientFactory$() {}

  private static AwsConfiguration config() {
    return Configuration$.newProxy(AwsConfiguration.class, "netflix.edda.aws");
  }

  private static AWSCredentialsProvider credentialsProvider(AwsConfiguration config) {
    String t = config.credentialsProviderType();
    if (t.equals("env"))
      return new EnvironmentVariableCredentialsProvider();
    else if (t.equals("role"))
      return new STSAssumeRoleSessionCredentialsProvider(config.roleArn(), config.roleSessionName());
    else if (t.equals("instance"))
      return new InstanceProfileCredentialsProvider();
    else if (t.equals("system"))
      return new SystemPropertiesCredentialsProvider();
    else if (t.startsWith("classpath:"))
      return new ClasspathPropertiesFileCredentialsProvider(t.substring(10));
    else if (t.startsWith("file:")) {
      try {
        java.io.File f = new java.io.File(t.substring(7));
        final PropertiesCredentials cred = new PropertiesCredentials(f);
        return new AWSCredentialsProvider() {
          @Override
          public AWSCredentials getCredentials() { return cred; }
          @Override
          public void refresh() {}
        };
      }
      catch (java.io.IOException e) {
        throw new IllegalArgumentException("file credentials provider failed [" + t + "]", e);
      }
    }
    throw new IllegalArgumentException("unknown credentials provider type [" + t + "]");
  }

  private static ClientConfiguration clientConfig(AwsConfiguration config) {
    return new ClientConfiguration()
      .withConnectionTimeout((int) config.connectionTimeout().getMillis())
      .withMaxConnections(config.maxConnections())
      .withMaxErrorRetry(config.maxErrorRetry())
      .withSocketTimeout((int) config.socketTimeout().getMillis());
  }

  public static AmazonAutoScaling newAutoScalingClient() {
    AwsConfiguration config = config();
    return newAutoScalingClient(config, credentialsProvider(config), NetflixEnvironment$.region());
  }

  public static AmazonAutoScaling newAutoScalingClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("AutoScaling mock not yet supported");
    if (config.useAdmin())
      throw new UnsupportedOperationException("AutoScaling admin not yet supported");
    if (config.readOnly()) EddaAutoScalingClient$.readOnly(config);
    AmazonAutoScaling client = new AmazonAutoScalingClient(provider, clientConfig(config));
    client.setEndpoint("autoscaling." + region + ".amazonaws.com");
    if (config.useEdda())
      client = EddaAutoScalingClient$.wrap(client, config);
    return client;
  }

  public static AmazonEC2 newEc2Client() {
    AwsConfiguration config = config();
    return newEc2Client(config, credentialsProvider(config), NetflixEnvironment$.region());
  }

  public static AmazonEC2 newEc2Client(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("EC2 mock not yet supported");
    if (config.useAdmin())
      throw new UnsupportedOperationException("EC2 admin not yet supported");
    if (config.readOnly()) EddaEc2Client$.readOnly(config);
    AmazonEC2 client = new AmazonEC2Client(provider, clientConfig(config));
    client.setEndpoint("ec2." + region + ".amazonaws.com");
    if (config.useEdda())
      client = EddaEc2Client$.wrap(client, config);
    return client;
  }
}
