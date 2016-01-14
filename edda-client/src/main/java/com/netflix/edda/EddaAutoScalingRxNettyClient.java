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
      TypeReference<AutoScalingGroup> ref = new TypeReference<AutoScalingGroup>() {};
      String url = config.url() + "/api/v2/aws/autoScalingGroups;_expand";
      return doGet(ref, url).map(autoScalingGroups -> {
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
          0,
          null,
          new DescribeAutoScalingGroupsResult().withAutoScalingGroups(autoScalingGroups)
        );
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
      TypeReference<LaunchConfiguration> ref = new TypeReference<LaunchConfiguration>() {};
      String url = config.url() + "/api/v2/aws/launchConfigurations;_expand";
      return doGet(ref, url).map(launchConfigurations -> {
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
          0,
          null,
          new DescribeLaunchConfigurationsResult().withLaunchConfigurations(launchConfigurations)
        );
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
      TypeReference<ScalingPolicy> ref = new TypeReference<ScalingPolicy>() {};
      String url = config.url() + "/api/v2/aws/scalingPolicies;_expand";
      return doGet(ref, url).map(scalingPolicies -> {
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
          0,
          null,
          new DescribePoliciesResult().withScalingPolicies(scalingPolicies)
        );
      });
    });
  }
}
