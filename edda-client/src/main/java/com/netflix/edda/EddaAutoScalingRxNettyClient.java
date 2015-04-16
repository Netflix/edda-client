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

import com.fasterxml.jackson.core.type.TypeReference;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.autoscaling.AmazonAutoScalingRxNetty;
import com.amazonaws.services.autoscaling.model.*;

import com.amazonaws.services.ServiceResult;
import com.amazonaws.services.PaginatedServiceResult;

import rx.Observable;

public class EddaAutoScalingRxNettyClient extends EddaAwsRxNettyClient {
  public EddaAutoScalingRxNettyClient(AwsConfiguration config, String vip, String region) {
    super(config, vip, region);
  }

  public AmazonAutoScalingRxNetty readOnly() {
    return readOnly(AmazonAutoScalingRxNetty.class);
  }

  public AmazonAutoScalingRxNetty wrapAwsClient(AmazonAutoScalingRxNetty delegate) {
    return wrapAwsClient(AmazonAutoScalingRxNetty.class, delegate);
  }

  public Observable<PaginatedServiceResult<DescribeAutoScalingGroupsResult>> describeAutoScalingGroups() {
    return describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest());
  }

  public Observable<PaginatedServiceResult<DescribeAutoScalingGroupsResult>> describeAutoScalingGroups(
    final DescribeAutoScalingGroupsRequest request
  ) {
    return Observable.defer(() -> {
      TypeReference<List<AutoScalingGroup>> ref = new TypeReference<List<AutoScalingGroup>>() {};
      String url = config.url() + "/api/v2/aws/autoScalingGroups;_expand";
      return doGet(url).map(sr -> {
        try {
          List<AutoScalingGroup> autoScalingGroups = parse(ref, sr.result);

          List<String> names = request.getAutoScalingGroupNames();
          if (shouldFilter(names)) {
            List<AutoScalingGroup> asgs = new ArrayList<AutoScalingGroup>();
            for (AutoScalingGroup asg : autoScalingGroups) {
              if (matches(names, asg.getAutoScalingGroupName()))
                asgs.add(asg);
            }
            autoScalingGroups = asgs;
          }

          return new PaginatedServiceResult<DescribeAutoScalingGroupsResult>(
            sr.startTime,
            null,
            new DescribeAutoScalingGroupsResult().withAutoScalingGroups(autoScalingGroups)
          );
        }
        catch (IOException e) {
          throw new AmazonClientException("Faled to parse " + url, e);
        }
      });
    });
  }

  public Observable<PaginatedServiceResult<DescribeLaunchConfigurationsResult>> describeLaunchConfigurations() {
    return describeLaunchConfigurations(new DescribeLaunchConfigurationsRequest());
  }

  public Observable<PaginatedServiceResult<DescribeLaunchConfigurationsResult>> describeLaunchConfigurations(
    final DescribeLaunchConfigurationsRequest request
  ) {
    return Observable.defer(() -> {
      TypeReference<List<LaunchConfiguration>> ref = new TypeReference<List<LaunchConfiguration>>() {};
      String url = config.url() + "/api/v2/aws/launchConfigurations;_expand";
      return doGet(url).map(sr -> {
        try {
          List<LaunchConfiguration> launchConfigurations = parse(ref, sr.result);

          List<String> names = request.getLaunchConfigurationNames();
          if (shouldFilter(names)) {
            List<LaunchConfiguration> lcs = new ArrayList<LaunchConfiguration>();
            for (LaunchConfiguration lc : launchConfigurations) {
              if (matches(names, lc.getLaunchConfigurationName()))
                lcs.add(lc);
            }
            launchConfigurations = lcs;
          }

          return new PaginatedServiceResult<DescribeLaunchConfigurationsResult>(
            sr.startTime,
            null,
            new DescribeLaunchConfigurationsResult().withLaunchConfigurations(launchConfigurations)
          );
        }
        catch (IOException e) {
          throw new AmazonClientException("Faled to parse " + url, e);
        }
      });
    });
  }

  public Observable<PaginatedServiceResult<DescribePoliciesResult>> describePolicies() {
    return describePolicies(new DescribePoliciesRequest());
  }

  public Observable<PaginatedServiceResult<DescribePoliciesResult>> describePolicies(
    final DescribePoliciesRequest request
  ) {
    return Observable.defer(() -> {
      TypeReference<List<ScalingPolicy>> ref = new TypeReference<List<ScalingPolicy>>() {};
      String url = config.url() + "/api/v2/aws/scalingPolicies;_expand";
      return doGet(url).map(sr -> {
        try {
          List<ScalingPolicy> scalingPolicies = parse(ref, sr.result);

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

          return new PaginatedServiceResult<DescribePoliciesResult>(
            sr.startTime,
            null,
            new DescribePoliciesResult().withScalingPolicies(scalingPolicies)
          );
        }
        catch (IOException e) {
          throw new AmazonClientException("Faled to parse " + url, e);
        }
      });
    });
  }
}
