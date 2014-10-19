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

import java.util.concurrent.atomic.AtomicReference;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
//import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
//import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
//import com.amazonaws.auth.InstanceProfileCredentialsProvider;
//import com.amazonaws.auth.PropertiesCredentials;
//import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
//import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;

import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;

import com.netflix.ie.config.Configuration;
import com.netflix.ie.platform.NetflixEnvironment;

public class AwsClientFactory {
  private AwsClientFactory() {}

  private static AwsConfiguration config() {
    return Configuration.newProxy(AwsConfiguration.class, "netflix.edda.aws");
  }

  private static final AtomicReference<AWSCredentialsProvider> DEFAULT_PROVIDER =
    new AtomicReference<AWSCredentialsProvider>(new DefaultAWSCredentialsProviderChain());

  public static void setDefaultCredentialProvider(AWSCredentialsProvider p) {
    DEFAULT_PROVIDER.set(p);
  }

/**
  private static AWSCredentialsProvider credentialsProvider(AwsConfiguration config) {
    String t = config.credentialsProviderType();
    AWSCredentialsProvider p = null
    if (t.equals("env"))
      p = new EnvironmentVariableCredentialsProvider();
    else if (t.equals("instance"))
      p = new InstanceProfileCredentialsProvider();
    else if (t.equals("system"))
      p = new SystemPropertiesCredentialsProvider();
    else if (t.startsWith("classpath:"))
      p = new ClasspathPropertiesFileCredentialsProvider(t.substring(10));
    else if (t.startsWith("file:")) {
      try {
        java.io.File f = new java.io.File(t.substring(7));
        final PropertiesCredentials cred = new PropertiesCredentials(f);
        p = new AWSCredentialsProvider() {
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
    else
      throw new IllegalArgumentException("unknown credentials provider type [" + t + "]");

    if (config.roleArn().length() > 0 && config.roleSessionName().length() > 0)
      p = new STSAssumeRoleSessionCredentialsProvider(p, config.roleArn(), config.roleSessionName());
    return p;
  }
*/

  private static ClientConfiguration clientConfig(AwsConfiguration config) {
    return new ClientConfiguration()
      .withConnectionTimeout((int) config.connectionTimeout().getMillis())
      .withMaxConnections(config.maxConnections())
      .withMaxErrorRetry(config.maxErrorRetry())
      .withSocketTimeout((int) config.socketTimeout().getMillis());
  }

  public static AmazonAutoScaling newAutoScalingClient() {
    AwsConfiguration config = config();
    return newAutoScalingClient(config, DEFAULT_PROVIDER.get(), NetflixEnvironment.region());
  }

  public static AmazonAutoScaling newAutoScalingClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("AutoScaling mock not yet supported");
    if (!config.wrapAwsClient())
      return new EddaAutoScalingClient(config).readOnly();

    AmazonAutoScaling client = new AmazonAutoScalingClient(provider, clientConfig(config));
    client.setEndpoint("autoscaling." + region + ".amazonaws.com");
    if (config.useEdda())
      client = new EddaAutoScalingClient(config).wrapAwsClient(client);
    return client;
  }

  public static AmazonCloudWatch newCloudWatchClient() {
    AwsConfiguration config = config();
    return newCloudWatchClient(config, DEFAULT_PROVIDER.get(), NetflixEnvironment.region());
  }

  public static AmazonCloudWatch newCloudWatchClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("CloudWatch mock not yet supported");
    if (!config.wrapAwsClient())
      return new EddaCloudWatchClient(config).readOnly();

    AmazonCloudWatch client = new AmazonCloudWatchClient(provider, clientConfig(config));
    client.setEndpoint("monitoring." + region + ".amazonaws.com");
    if (config.useEdda())
      client = new EddaCloudWatchClient(config).wrapAwsClient(client);
    return client;
  }

  public static AmazonEC2 newEc2Client() {
    AwsConfiguration config = config();
    return newEc2Client(config, DEFAULT_PROVIDER.get(), NetflixEnvironment.region());
  }

  public static AmazonEC2 newEc2Client(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("EC2 mock not yet supported");
    if (!config.wrapAwsClient())
      return new EddaEc2Client(config).readOnly();

    AmazonEC2 client = new AmazonEC2Client(provider, clientConfig(config));
    client.setEndpoint("ec2." + region + ".amazonaws.com");
    if (config.useEdda())
      client = new EddaEc2Client(config).wrapAwsClient(client);
    return client;
  }

  public static AmazonElasticLoadBalancing newElasticLoadBalancingClient() {
    AwsConfiguration config = config();
    return newElasticLoadBalancingClient(config, DEFAULT_PROVIDER.get(), NetflixEnvironment.region());
  }

  public static AmazonElasticLoadBalancing newElasticLoadBalancingClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("ElasticLoadBalancing mock not yet supported");
    if (!config.wrapAwsClient())
      return new EddaElasticLoadBalancingClient(config).readOnly();

    AmazonElasticLoadBalancing client = new AmazonElasticLoadBalancingClient(provider, clientConfig(config));
    client.setEndpoint("elasticloadbalancing." + region + ".amazonaws.com");
    if (config.useEdda())
      client = new EddaElasticLoadBalancingClient(config).wrapAwsClient(client);
    return client;
  }

  public static AmazonRoute53 newRoute53Client() {
    AwsConfiguration config = config();
    return newRoute53Client(config, DEFAULT_PROVIDER.get(), NetflixEnvironment.region());
  }

  public static AmazonRoute53 newRoute53Client(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("Route53 mock not yet supported");
    if (!config.wrapAwsClient())
      return new EddaRoute53Client(config).readOnly();

    AmazonRoute53 client = new AmazonRoute53Client(provider, clientConfig(config));
    if (config.useEdda())
      client = new EddaRoute53Client(config).wrapAwsClient(client);
    return client;
  }
}
