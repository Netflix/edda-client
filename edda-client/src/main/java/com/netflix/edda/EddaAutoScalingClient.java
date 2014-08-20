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

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.model.*;

public class EddaAutoScalingClient extends EddaAwsClient {
  public EddaAutoScalingClient(AwsConfiguration config) {
    super(config);
  }

  public AmazonAutoScaling readOnly() {
    return readOnly(AmazonAutoScaling.class);
  }

  public AmazonAutoScaling wrapAwsClient(AmazonAutoScaling delegate) {
    return wrapAwsClient(AmazonAutoScaling.class, delegate);
  }

  public DescribeAutoScalingGroupsResult describeAutoScalingGroups() {
    return describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest());
  }

  public DescribeAutoScalingGroupsResult describeAutoScalingGroups(DescribeAutoScalingGroupsRequest request) {
    TypeReference<List<AutoScalingGroup>> ref = new TypeReference<List<AutoScalingGroup>>() {};
    String url = config.url() + "/api/v2/aws/autoScalingGroups;_expand";
    try {
      List<AutoScalingGroup> autoScalingGroups = parse(ref, doGet(url).body());
      return new DescribeAutoScalingGroupsResult()
        .withAutoScalingGroups(autoScalingGroups);
    }
    catch (IOException e) {
      throw new AmazonClientException("Faled to parse " + url, e);
    }
  }

  public DescribeLaunchConfigurationsResult describeLaunchConfigurations() {
    return describeLaunchConfigurations(new DescribeLaunchConfigurationsRequest());
  }

  public DescribeLaunchConfigurationsResult describeLaunchConfigurations(DescribeLaunchConfigurationsRequest request) {
    TypeReference<List<LaunchConfiguration>> ref = new TypeReference<List<LaunchConfiguration>>() {};
    String url = config.url() + "/api/v2/aws/launchConfigurations;_expand";
    try {
      List<LaunchConfiguration> launchConfigurations = parse(ref, doGet(url).body());
      return new DescribeLaunchConfigurationsResult()
        .withLaunchConfigurations(launchConfigurations);
    }
    catch (IOException e) {
      throw new AmazonClientException("Faled to parse " + url, e);
    }
  }

  public DescribePoliciesResult describePolicies() {
    return describePolicies(new DescribePoliciesRequest());
  }

  public DescribePoliciesResult describePolicies(DescribePoliciesRequest request) {
    TypeReference<List<ScalingPolicy>> ref = new TypeReference<List<ScalingPolicy>>() {};
    String url = config.url() + "/api/v2/aws/scalingPolicies;_expand";
    try {
      List<ScalingPolicy> scalingPolicies = parse(ref, doGet(url).body());
      return new DescribePoliciesResult()
        .withScalingPolicies(scalingPolicies);
    }
    catch (IOException e) {
      throw new AmazonClientException("Faled to parse " + url, e);
    }
  }
}
