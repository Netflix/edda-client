/*
 * Copyright 2014-2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.amazonaws.services.autoscaling.AmazonAutoScalingRxNetty;
import com.amazonaws.services.autoscaling.AmazonAutoScalingRxNettyClient;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchRxNetty;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchRxNettyClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2RxNetty;
import com.amazonaws.services.ec2.AmazonEC2RxNettyClient;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingRxNetty;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingRxNettyClient;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.AmazonRoute53RxNetty;
import com.amazonaws.services.route53.AmazonRoute53RxNettyClient;

import com.netflix.iep.config.Configuration;
import com.netflix.iep.NetflixEnvironment;

public class AwsClientFactory {
  private AwsClientFactory() {}

  private static AwsConfiguration config() {
    return Configuration.newProxy(AwsConfiguration.class, "netflix.edda.aws");
  }

  private static final AtomicReference<AWSCredentialsProvider> DEFAULT_PROVIDER =
    new AtomicReference<AWSCredentialsProvider>(new DefaultAWSCredentialsProviderChain());

  private static final AtomicReference<String>  DEFAULT_VIP =
    new AtomicReference<String>("edda-main:7001");

  public static void setDefaultCredentialsProvider(AWSCredentialsProvider p) {
    DEFAULT_PROVIDER.set(p);
  }

  public static void setDefaultVip(String vip) {
    DEFAULT_VIP.set(vip);
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
    return newAutoScalingClient(DEFAULT_PROVIDER.get(), DEFAULT_VIP.get());
  }

  public static AmazonAutoScaling newAutoScalingClient(AWSCredentialsProvider provider, String vip) {
    AwsConfiguration config = config();
    return newAutoScalingClient(config, provider, vip, NetflixEnvironment.region());
  }

  public static AmazonAutoScaling newAutoScalingClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String vip,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("AutoScaling mock not yet supported");

    EddaAutoScalingClient edda = new EddaAutoScalingClient(config, vip, region);

    if (config.useEdda() && !config.wrapAwsClient()) return edda.readOnly();

    AmazonAutoScaling client = new AmazonAutoScalingClient(provider, clientConfig(config));
    client.setEndpoint("autoscaling." + region + ".amazonaws.com");
    if (config.useEdda())
      client = edda.wrapAwsClient(client);
    return client;
  }

  public static AmazonAutoScalingRxNetty newAutoScalingRxNettyClient() {
    return newAutoScalingRxNettyClient(DEFAULT_PROVIDER.get(), DEFAULT_VIP.get());
  }

  public static AmazonAutoScalingRxNetty newAutoScalingRxNettyClient(AWSCredentialsProvider provider, String vip) {
    AwsConfiguration config = config();
    return newAutoScalingRxNettyClient(config, provider, vip, NetflixEnvironment.region());
  }

  public static AmazonAutoScalingRxNetty newAutoScalingRxNettyClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String vip,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("AutoScaling mock not yet supported");

    EddaAutoScalingRxNettyClient edda = new EddaAutoScalingRxNettyClient(config, vip, region);

    if (config.useEdda() && !config.wrapAwsClient()) return edda.readOnly();

    AmazonAutoScalingRxNetty client = new AmazonAutoScalingRxNettyClient(provider, clientConfig(config));
    client.setEndpoint("autoscaling." + region + ".amazonaws.com");
    if (config.useEdda())
      client = edda.wrapAwsClient(client);
    return client;
  }


  public static AmazonCloudWatch newCloudWatchClient() {
    return newCloudWatchClient(DEFAULT_PROVIDER.get(), DEFAULT_VIP.get());
  }

  public static AmazonCloudWatch newCloudWatchClient(AWSCredentialsProvider provider, String vip) {
    AwsConfiguration config = config();
    return newCloudWatchClient(config, provider, vip, NetflixEnvironment.region());
  }

  public static AmazonCloudWatch newCloudWatchClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String vip,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("CloudWatch mock not yet supported");

    EddaCloudWatchClient edda = new EddaCloudWatchClient(config, vip, region);

    if (config.useEdda() && !config.wrapAwsClient()) return edda.readOnly();

    AmazonCloudWatch client = new AmazonCloudWatchClient(provider, clientConfig(config));
    client.setEndpoint("monitoring." + region + ".amazonaws.com");
    if (config.useEdda())
      client = edda.wrapAwsClient(client);
    return client;
  }

  public static AmazonCloudWatchRxNetty newCloudWatchRxNettyClient() {
    return newCloudWatchRxNettyClient(DEFAULT_PROVIDER.get(), DEFAULT_VIP.get());
  }

  public static AmazonCloudWatchRxNetty newCloudWatchRxNettyClient(AWSCredentialsProvider provider, String vip) {
    AwsConfiguration config = config();
    return newCloudWatchRxNettyClient(config, provider, vip, NetflixEnvironment.region());
  }

  public static AmazonCloudWatchRxNetty newCloudWatchRxNettyClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String vip,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("CloudWatch mock not yet supported");

    EddaCloudWatchRxNettyClient edda = new EddaCloudWatchRxNettyClient(config, vip, region);

    if (config.useEdda() && !config.wrapAwsClient()) return edda.readOnly();

    AmazonCloudWatchRxNetty client = new AmazonCloudWatchRxNettyClient(provider, clientConfig(config));
    client.setEndpoint("monitoring." + region + ".amazonaws.com");
    if (config.useEdda())
      client = edda.wrapAwsClient(client);
    return client;
  }


  public static AmazonEC2 newEc2Client() {
    return newEc2Client(DEFAULT_PROVIDER.get(), DEFAULT_VIP.get());
  }

  public static AmazonEC2 newEc2Client(AWSCredentialsProvider provider, String vip) {
    AwsConfiguration config = config();
    return newEc2Client(config, provider, vip, NetflixEnvironment.region());
  }

  public static AmazonEC2 newEc2Client(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String vip,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("EC2 mock not yet supported");

    EddaEc2Client edda = new EddaEc2Client(config, vip, region);
    if (config.useEdda() && !config.wrapAwsClient()) return edda.readOnly();

    AmazonEC2 client = new AmazonEC2Client(provider, clientConfig(config));
    client.setEndpoint("ec2." + region + ".amazonaws.com");
    if (config.useEdda())
      client = edda.wrapAwsClient(client);
    return client;
  }

  public static AmazonEC2RxNetty newEc2RxNettyClient() {
    return newEc2RxNettyClient(DEFAULT_PROVIDER.get(), DEFAULT_VIP.get());
  }

  public static AmazonEC2RxNetty newEc2RxNettyClient(AWSCredentialsProvider provider, String vip) {
    AwsConfiguration config = config();
    return newEc2RxNettyClient(config, provider, vip, NetflixEnvironment.region());
  }

  public static AmazonEC2RxNetty newEc2RxNettyClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String vip,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("EC2 mock not yet supported");

    EddaEc2RxNettyClient edda = new EddaEc2RxNettyClient(config, vip, region);
    if (config.useEdda() && !config.wrapAwsClient()) return edda.readOnly();

    AmazonEC2RxNetty client = new AmazonEC2RxNettyClient(provider, clientConfig(config));
    client.setEndpoint("ec2." + region + ".amazonaws.com");
    if (config.useEdda())
      client = edda.wrapAwsClient(client);
    return client;
  }


  public static AmazonElasticLoadBalancing newElasticLoadBalancingClient() {
    return newElasticLoadBalancingClient(DEFAULT_PROVIDER.get(), DEFAULT_VIP.get());
  }

  public static AmazonElasticLoadBalancing newElasticLoadBalancingClient(
    AWSCredentialsProvider provider,
    String vip
  ) {
    AwsConfiguration config = config();
    return newElasticLoadBalancingClient(config, provider, vip, NetflixEnvironment.region());
  }

  public static AmazonElasticLoadBalancing newElasticLoadBalancingClient(
    AwsConfiguration config
  ) {
    AWSCredentialsProvider provider = DEFAULT_PROVIDER.get();
    String vip = DEFAULT_VIP.get();
    return newElasticLoadBalancingClient(config, provider, vip, NetflixEnvironment.region());
  }

  public static AmazonElasticLoadBalancing newElasticLoadBalancingClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String vip,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("ElasticLoadBalancing mock not yet supported");

    EddaElasticLoadBalancingClient edda = new EddaElasticLoadBalancingClient(config, vip, region);

    if (config.useEdda() && !config.wrapAwsClient()) return edda.readOnly();

    AmazonElasticLoadBalancing client = new AmazonElasticLoadBalancingClient(provider, clientConfig(config));
    client.setEndpoint("elasticloadbalancing." + region + ".amazonaws.com");
    if (config.useEdda())
      client = edda.wrapAwsClient(client);
    return client;
  }

  public static AmazonElasticLoadBalancingRxNetty newElasticLoadBalancingRxNettyClient() {
    return newElasticLoadBalancingRxNettyClient(DEFAULT_PROVIDER.get(), DEFAULT_VIP.get());
  }

  public static AmazonElasticLoadBalancingRxNetty newElasticLoadBalancingRxNettyClient(
    AWSCredentialsProvider provider,
    String vip
  ) {
    AwsConfiguration config = config();
    return newElasticLoadBalancingRxNettyClient(config, provider, vip, NetflixEnvironment.region());
  }

  public static AmazonElasticLoadBalancingRxNetty newElasticLoadBalancingRxNettyClient(
    AwsConfiguration config
  ) {
    AWSCredentialsProvider provider = DEFAULT_PROVIDER.get();
    String vip = DEFAULT_VIP.get();
    return newElasticLoadBalancingRxNettyClient(config, provider, vip, NetflixEnvironment.region());
  }

  public static AmazonElasticLoadBalancingRxNetty newElasticLoadBalancingRxNettyClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String vip,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("ElasticLoadBalancing mock not yet supported");

    EddaElasticLoadBalancingRxNettyClient edda = new EddaElasticLoadBalancingRxNettyClient(config, vip, region);

    if (config.useEdda() && !config.wrapAwsClient()) return edda.readOnly();

    AmazonElasticLoadBalancingRxNetty client = new AmazonElasticLoadBalancingRxNettyClient(provider, clientConfig(config));
    client.setEndpoint("elasticloadbalancing." + region + ".amazonaws.com");
    if (config.useEdda())
      client = edda.wrapAwsClient(client);
    return client;
  }


  public static AmazonRoute53 newRoute53Client() {
    return newRoute53Client(DEFAULT_PROVIDER.get(), DEFAULT_VIP.get());
  }

  public static AmazonRoute53 newRoute53Client(AWSCredentialsProvider provider, String vip) {
    AwsConfiguration config = config();
    return newRoute53Client(config, provider, vip, NetflixEnvironment.region());
  }

  public static AmazonRoute53 newRoute53Client(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String vip,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("Route53 mock not yet supported");

    EddaRoute53Client edda = new EddaRoute53Client(config, vip, region);

    if (config.useEdda() && !config.wrapAwsClient()) return edda.readOnly();

    AmazonRoute53 client = new AmazonRoute53Client(provider, clientConfig(config));
    if (config.useEdda())
      client = edda.wrapAwsClient(client);
    return client;
  }

  public static AmazonRoute53RxNetty newRoute53RxNettyClient() {
    return newRoute53RxNettyClient(DEFAULT_PROVIDER.get(), DEFAULT_VIP.get());
  }

  public static AmazonRoute53RxNetty newRoute53RxNettyClient(AWSCredentialsProvider provider, String vip) {
    AwsConfiguration config = config();
    return newRoute53RxNettyClient(config, provider, vip, NetflixEnvironment.region());
  }

  public static AmazonRoute53RxNetty newRoute53RxNettyClient(
    AwsConfiguration config,
    AWSCredentialsProvider provider,
    String vip,
    String region
  ) {
    if (config.useMock())
      throw new UnsupportedOperationException("Route53 mock not yet supported");

    EddaRoute53RxNettyClient edda = new EddaRoute53RxNettyClient(config, vip, region);

    if (config.useEdda() && !config.wrapAwsClient()) return edda.readOnly();

    AmazonRoute53RxNetty client = new AmazonRoute53RxNettyClient(provider, clientConfig(config));
    if (config.useEdda())
      client = edda.wrapAwsClient(client);
    return client;
  }
}
