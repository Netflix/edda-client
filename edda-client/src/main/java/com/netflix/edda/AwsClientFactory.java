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
import com.amazonaws.ClientConfiguration;

//import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
//import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
//import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
//import com.amazonaws.auth.InstanceProfileCredentialsProvider;
//import com.amazonaws.auth.PropertiesCredentials;
//import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
//import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;

import com.amazonaws.handlers.RequestHandler2;
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

  public static void setDefaultCredentialsProvider(AWSCredentialsProvider p) {
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
    return newAutoScalingClient(DEFAULT_PROVIDER.get());
  }

  public static AmazonAutoScaling newAutoScalingClient(AWSCredentialsProvider provider) {
    AwsConfiguration config = config();
    return newAutoScalingClient(config, provider, NetflixEnvironment.region());
  }

  public static AmazonAutoScaling newAutoScalingClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region
  ) {
    return newAutoScalingClient(config, provider, region, null);
  }

  public static AmazonAutoScaling newAutoScalingClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region,
    RequestHandler2 responseCallback
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("AutoScaling mock not yet supported");
    if (!config.wrapAwsClient())
      return new EddaAutoScalingClient(config, responseCallback).readOnly();

    AmazonAutoScalingClient client = new AmazonAutoScalingClient(provider, clientConfig(config));
    client.setEndpoint("autoscaling." + region + ".amazonaws.com");
    if (responseCallback != null) {
      client.addRequestHandler(responseCallback);
    }
    if (config.useEdda())
      return new EddaAutoScalingClient(config, responseCallback).wrapAwsClient(client);

    return client;
  }

  public static AmazonCloudWatch newCloudWatchClient() {
    return newCloudWatchClient(DEFAULT_PROVIDER.get());
  }

  public static AmazonCloudWatch newCloudWatchClient(AWSCredentialsProvider provider) {
    AwsConfiguration config = config();
    return newCloudWatchClient(config, provider, NetflixEnvironment.region());
  }

  public static AmazonCloudWatch newCloudWatchClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region
  ) {
    return newCloudWatchClient(config, provider, region, null);
  }

  public static AmazonCloudWatch newCloudWatchClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region,
    RequestHandler2 responseCallback
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("CloudWatch mock not yet supported");
    if (!config.wrapAwsClient())
      return new EddaCloudWatchClient(config, responseCallback).readOnly();

    AmazonCloudWatchClient client = new AmazonCloudWatchClient(provider, clientConfig(config));
    client.setEndpoint("monitoring." + region + ".amazonaws.com");
    if (responseCallback != null) {
      client.addRequestHandler(responseCallback);
    }
    if (config.useEdda())
      return new EddaCloudWatchClient(config, responseCallback).wrapAwsClient(client);

    return client;
  }

  public static AmazonEC2 newEc2Client() {
    return newEc2Client(DEFAULT_PROVIDER.get());
  }

  public static AmazonEC2 newEc2Client(AWSCredentialsProvider provider) {
    AwsConfiguration config = config();
    return newEc2Client(config, provider, NetflixEnvironment.region());
  }

  public static AmazonEC2 newEc2Client(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region
  ) {
    return newEc2Client(config, provider, region, null);
  }

  public static AmazonEC2 newEc2Client(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region,
    RequestHandler2 responseCallback
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("EC2 mock not yet supported");
    if (!config.wrapAwsClient())
      return new EddaEc2Client(config, responseCallback).readOnly();

    AmazonEC2Client client = new AmazonEC2Client(provider, clientConfig(config));
    client.setEndpoint("ec2." + region + ".amazonaws.com");
    if (responseCallback != null) {
      client.addRequestHandler(responseCallback);
    }
    if (config.useEdda())
      return new EddaEc2Client(config, responseCallback).wrapAwsClient(client);

    return client;
  }

  public static AmazonElasticLoadBalancing newElasticLoadBalancingClient() {
    return newElasticLoadBalancingClient(DEFAULT_PROVIDER.get());
  }

  public static AmazonElasticLoadBalancing newElasticLoadBalancingClient(
    AWSCredentialsProvider provider
  ) {
    AwsConfiguration config = config();
    return newElasticLoadBalancingClient(config, provider, NetflixEnvironment.region());
  }

  public static AmazonElasticLoadBalancing newElasticLoadBalancingClient(
    AwsConfiguration config
  ) {
    AWSCredentialsProvider provider = DEFAULT_PROVIDER.get();
    return newElasticLoadBalancingClient(config, provider, NetflixEnvironment.region());
  }

  public static AmazonElasticLoadBalancing newElasticLoadBalancingClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region
  ) {
    return newElasticLoadBalancingClient(config, provider, region, null);
  }

  public static AmazonElasticLoadBalancing newElasticLoadBalancingClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region,
    RequestHandler2 responseCallback
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("ElasticLoadBalancing mock not yet supported");
    if (!config.wrapAwsClient())
      return new EddaElasticLoadBalancingClient(config, responseCallback).readOnly();

    AmazonElasticLoadBalancingClient client = new AmazonElasticLoadBalancingClient(provider, clientConfig(config));
    client.setEndpoint("elasticloadbalancing." + region + ".amazonaws.com");
    if (responseCallback != null) {
      client.addRequestHandler(responseCallback);
    }
    if (config.useEdda())
      return new EddaElasticLoadBalancingClient(config, responseCallback).wrapAwsClient(client);

    return client;
  }

  public static AmazonRoute53 newRoute53Client() {
    return newRoute53Client(DEFAULT_PROVIDER.get());
  }

  public static AmazonRoute53 newRoute53Client(AWSCredentialsProvider provider) {
    AwsConfiguration config = config();
    return newRoute53Client(config, provider, NetflixEnvironment.region());
  }

  public static AmazonRoute53 newRoute53Client(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region
  ) {
    return newRoute53Client(config, provider, region, null);
  }

  public static AmazonRoute53 newRoute53Client(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String region,
    RequestHandler2 responseCallback
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("Route53 mock not yet supported");
    if (!config.wrapAwsClient())
      return new EddaRoute53Client(config, responseCallback).readOnly();

    AmazonRoute53Client client = new AmazonRoute53Client(provider, clientConfig(config));
    if (responseCallback != null) {
      client.addRequestHandler(responseCallback);
    }
    if (config.useEdda())
      return new EddaRoute53Client(config, responseCallback).wrapAwsClient(client);

    return client;
  }
}
