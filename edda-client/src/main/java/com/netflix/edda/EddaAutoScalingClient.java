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
import java.util.ArrayList;

import com.amazonaws.handlers.RequestHandler2;
import com.fasterxml.jackson.core.type.TypeReference;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.model.*;

public class EddaAutoScalingClient extends EddaAwsClient {
  public EddaAutoScalingClient(AwsConfiguration config) {
    super(config);
  }

  public EddaAutoScalingClient(AwsConfiguration config, RequestHandler2 responseCallback) {
    super(config, responseCallback);
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
    EddaRequest<DescribeAutoScalingGroupsRequest> req = buildRequest(request, "/api/v2/aws/autoScalingGroups;_expand");
    EddaResponse response = doGet(req);
    try {
      List<AutoScalingGroup> autoScalingGroups = parse(ref, response);

      List<String> names = request.getAutoScalingGroupNames();
      if (shouldFilter(names)) {
        List<AutoScalingGroup> asgs = new ArrayList<AutoScalingGroup>();
        for (AutoScalingGroup asg : autoScalingGroups) {
          if (matches(names, asg.getAutoScalingGroupName()))
            asgs.add(asg);
        }
        autoScalingGroups = asgs;
      }

      return handleResponse(response, req, new DescribeAutoScalingGroupsResult()
        .withAutoScalingGroups(autoScalingGroups));
    }
    catch (IOException e) {
      response.setException(new AmazonClientException("Failed to parse " + req.getUrl(), e));
      throw notifyException(req, response);
    }
  }

  public DescribeLaunchConfigurationsResult describeLaunchConfigurations() {
    return describeLaunchConfigurations(new DescribeLaunchConfigurationsRequest());
  }

  public DescribeLaunchConfigurationsResult describeLaunchConfigurations(DescribeLaunchConfigurationsRequest request) {
    TypeReference<List<LaunchConfiguration>> ref = new TypeReference<List<LaunchConfiguration>>() {};
    EddaRequest<DescribeLaunchConfigurationsRequest> req = buildRequest(request, "/api/v2/aws/launchConfigurations;_expand");
    EddaResponse response = doGet(req);
    try {
      List<LaunchConfiguration> launchConfigurations = parse(ref, response);

      List<String> names = request.getLaunchConfigurationNames();
      if (shouldFilter(names)) {
        List<LaunchConfiguration> lcs = new ArrayList<LaunchConfiguration>();
        for (LaunchConfiguration lc : launchConfigurations) {
          if (matches(names, lc.getLaunchConfigurationName()))
            lcs.add(lc);
        }
        launchConfigurations = lcs;
      }

      return handleResponse(response, req, new DescribeLaunchConfigurationsResult()
        .withLaunchConfigurations(launchConfigurations));
    }
    catch (IOException e) {
      response.setException(new AmazonClientException("Failed to parse " + req.getUrl(), e));
      throw notifyException(req, response);
    }
  }

  public DescribePoliciesResult describePolicies() {
    return describePolicies(new DescribePoliciesRequest());
  }

  public DescribePoliciesResult describePolicies(DescribePoliciesRequest request) {
    TypeReference<List<ScalingPolicy>> ref = new TypeReference<List<ScalingPolicy>>() {};
    EddaRequest<DescribePoliciesRequest> req = buildRequest(request, "/api/v2/aws/scalingPolicies;_expand");
    EddaResponse response = doGet(req);
    try {
      List<ScalingPolicy> scalingPolicies = parse(ref, response);

      String asg = request.getAutoScalingGroupName();
      List<String> names = request.getPolicyNames();
      if (shouldFilter(asg) || shouldFilter(names)) {
        List<ScalingPolicy> sps = new ArrayList<ScalingPolicy>();
        for (ScalingPolicy sp : scalingPolicies) {
          if (matches(asg, sp.getAutoScalingGroupName()) && matches(names, sp.getPolicyName()))
            sps.add(sp);
        }
        scalingPolicies = sps;
      }

      return handleResponse(response, req, new DescribePoliciesResult()
        .withScalingPolicies(scalingPolicies));
    }
    catch (IOException e) {
      response.setException(new AmazonClientException("Failed to parse " + req.getUrl(), e));
      throw notifyException(req, response);
    }
  }
}
