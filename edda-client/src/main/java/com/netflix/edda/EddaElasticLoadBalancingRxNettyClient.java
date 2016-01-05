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

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.core.type.TypeReference;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingRxNetty;
import com.amazonaws.services.elasticloadbalancing.model.*;

import com.netflix.edda.mapper.InstanceStateView;
import com.netflix.edda.mapper.LoadBalancerAttributesView;

import com.amazonaws.services.ServiceResult;
import com.amazonaws.services.NamedServiceResult;
import com.amazonaws.services.PaginatedServiceResult;

import rx.Observable;

public class EddaElasticLoadBalancingRxNettyClient extends EddaAwsRxNettyClient {
  public EddaElasticLoadBalancingRxNettyClient(AwsConfiguration config, String vip, String region) {
    super(config, vip, region);
  }

  public AmazonElasticLoadBalancingRxNetty readOnly() {
    return readOnly(AmazonElasticLoadBalancingRxNetty.class);
  }

  public AmazonElasticLoadBalancingRxNetty wrapAwsClient(AmazonElasticLoadBalancingRxNetty delegate) {
    return wrapAwsClient(AmazonElasticLoadBalancingRxNetty.class, delegate);
  }

  public Observable<NamedServiceResult<DescribeInstanceHealthResult>> describeInstanceHealth() {
    return Observable.defer(() -> {
      TypeReference<InstanceStateView> ref = new TypeReference<InstanceStateView>() {};
      String url = config.url() + "/api/v2/view/loadBalancerInstances;_expand";
      return doGet(ref, url).flatMap(vs -> {
          return Observable.from(vs).map(view -> {
            return new NamedServiceResult<DescribeInstanceHealthResult>(
              0,
              view.getName(),
              new DescribeInstanceHealthResult().withInstanceStates(view.getInstances())
            );
          });
      });
    });
  }

  public Observable<ServiceResult<DescribeInstanceHealthResult>> describeInstanceHealth(
    final DescribeInstanceHealthRequest request
  ) {
    return Observable.defer(() -> {
      validateNotEmpty("LoadBalancerName", request.getLoadBalancerName());

      TypeReference<InstanceStateView> ref = new TypeReference<InstanceStateView>() {};
      String loadBalancerName = request.getLoadBalancerName();
    
      String url = config.url() + "/api/v2/view/loadBalancerInstances/"+loadBalancerName+";_expand";
      return doGet(ref, url).flatMap(vs -> {
        return Observable.from(vs).map(view -> {
          List<InstanceState> instanceStates = view.getInstances();

          List<Instance> instances = request.getInstances();
          List<String> ids = new ArrayList<String>();
          if (instances != null) {
            for (Instance i : instances)
              ids.add(i.getInstanceId());
          }
          if (shouldFilter(ids)) {
            List<InstanceState> iss = new ArrayList<InstanceState>();
            for (InstanceState is : instanceStates) {
              if (matches(ids, is.getInstanceId()))
                iss.add(is);
            }
            instanceStates = iss;
          }

          return new ServiceResult<DescribeInstanceHealthResult>(
            0,
            new DescribeInstanceHealthResult().withInstanceStates(instanceStates)
          );
        });
      });
    });
  }

  public Observable<PaginatedServiceResult<DescribeLoadBalancersResult>> describeLoadBalancers() {
    return describeLoadBalancers(new DescribeLoadBalancersRequest());
  }

  public Observable<PaginatedServiceResult<DescribeLoadBalancersResult>> describeLoadBalancers(
    final DescribeLoadBalancersRequest request
  ) {
    return Observable.defer(() -> {
      TypeReference<LoadBalancerDescription> ref = new TypeReference<LoadBalancerDescription>() {};
      String url = config.url() + "/api/v2/aws/loadBalancers;_expand";
      return doGet(ref, url).map(loadBalancerDescriptions -> {
          List<String> names = request.getLoadBalancerNames();
          if (shouldFilter(names)) {
            List<LoadBalancerDescription> lbs = new ArrayList<LoadBalancerDescription>();
            for (LoadBalancerDescription lb : loadBalancerDescriptions) {
              if (matches(names, lb.getLoadBalancerName()))
                lbs.add(lb);
            }
            loadBalancerDescriptions = lbs;
          }

          return new PaginatedServiceResult<DescribeLoadBalancersResult>(
            0,
            null,
            new DescribeLoadBalancersResult().withLoadBalancerDescriptions(loadBalancerDescriptions)
          );
      });
    });
  }

  public Observable<NamedServiceResult<DescribeLoadBalancerAttributesResult>> describeLoadBalancerAttributes() {
    return Observable.defer(() -> {
      TypeReference<LoadBalancerAttributesView> ref = new TypeReference<LoadBalancerAttributesView>() {};
      String url = config.url() + "/api/v2/view/loadBalancerAttributes;_expand";
      return doGet(ref, url).flatMap(vs -> {
          return Observable.from(vs).map(view -> {
            return new NamedServiceResult<DescribeLoadBalancerAttributesResult>(
              0,
              view.getName(),
              new DescribeLoadBalancerAttributesResult().withLoadBalancerAttributes(view.getAttributes())
            );
          });
      });
    });
  }

  public Observable<ServiceResult<DescribeLoadBalancerAttributesResult>> describeLoadBalancerAttributes(
    final DescribeLoadBalancerAttributesRequest request
  ) {
    return Observable.defer(() -> {
      validateNotEmpty("LoadBalancerName", request.getLoadBalancerName());
      TypeReference<LoadBalancerAttributesView> ref = new TypeReference<LoadBalancerAttributesView>() {};
      String loadBalancerName = request.getLoadBalancerName();
      String url = config.url() + "/api/v2/view/loadBalancerAttributes/"+loadBalancerName+";_expand";
      return doGet(ref, url).flatMap(vs -> {
        return Observable.from(vs).map(view -> {
          return new ServiceResult<DescribeLoadBalancerAttributesResult>(
            0,
            new DescribeLoadBalancerAttributesResult().withLoadBalancerAttributes(view.getAttributes())
          );
        });
      });
    });
  }
}
