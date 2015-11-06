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
import com.amazonaws.services.ec2.AmazonEC2RxNetty;
import com.amazonaws.services.ec2.model.*;

import com.amazonaws.services.ServiceResult;
import com.amazonaws.services.PaginatedServiceResult;

import rx.Observable;

public class EddaEc2RxNettyClient extends EddaAwsRxNettyClient {
  public EddaEc2RxNettyClient(AwsConfiguration config, String vip, String region) {
    super(config, vip, region);
  }

  public AmazonEC2RxNetty readOnly() {
    return readOnly(AmazonEC2RxNetty.class);
  }

  public AmazonEC2RxNetty wrapAwsClient(AmazonEC2RxNetty delegate) {
    return wrapAwsClient(AmazonEC2RxNetty.class, delegate);
  }

  public Observable<PaginatedServiceResult<DescribeClassicLinkInstancesResult>> describeClassicLinkInstances() {
    return describeClassicLinkInstances(new DescribeClassicLinkInstancesRequest());
  }

  public Observable<PaginatedServiceResult<DescribeClassicLinkInstancesResult>> describeClassicLinkInstances(
    final DescribeClassicLinkInstancesRequest request
  ) {
    return Observable.defer(() -> {
      validateEmpty("Filter", request.getFilters());

      TypeReference<ClassicLinkInstance> ref = new TypeReference<ClassicLinkInstance>(){
      };

      String url = config.url() + "/api/v2/aws/classicLinkInstances;_expand";
      return doGetList(ref, url)
      .map(sr -> {
        List<ClassicLinkInstance> instances = sr.result;
        List<String> ids = request.getInstanceIds();
        if (shouldFilter(ids)) {
          List<ClassicLinkInstance> is = new ArrayList<ClassicLinkInstance>();
          for (ClassicLinkInstance i : instances) {
            if (matches(ids, i.getInstanceId()))
              is.add(i);
          }
          instances = is;
        }

        return new PaginatedServiceResult<DescribeClassicLinkInstancesResult>(
          sr.startTime,
          null,
          new DescribeClassicLinkInstancesResult().withInstances(instances)
        );
      });
    });
  }

  public Observable<ServiceResult<DescribeImagesResult>> describeImages() {
    return describeImages(new DescribeImagesRequest());
  }

  public Observable<ServiceResult<DescribeImagesResult>> describeImages(
    final DescribeImagesRequest request
  ) {
    return Observable.defer(() -> {
      validateEmpty("ExecutableUsers", request.getExecutableUsers());
      List<Filter> filters = request.getFilters();
      String path = "aws/images";
      if (filters != null && filters.size() > 0) {
        if (
          filters.size() == 1 &&
          filters.get(0) != null &&
          "is-public".equals(filters.get(0).getName()) &&
           filters.get(0).getValues() != null &&
           filters.get(0).getValues().size() == 1 &&
           "false".equals(filters.get(0).getValues().get(0))
        ) {
          path = "view/images";
        }
        else {
          throw new UnsupportedOperationException("filters only support is-public=false");
        }
      }

      TypeReference<Image> ref = new TypeReference<Image>() {};
      String url = config.url() + "/api/v2/" + path + ";_expand";
      return doGetList(ref, url)
      .map(sr -> {
        List<Image> images = sr.result;

        List<String> owners = request.getOwners();
        List<String> ids = request.getImageIds();
        if (shouldFilter(owners) || shouldFilter(ids)) {
          List<Image> is = new ArrayList<Image>();
          for (Image i : images) {
            if (matches(owners, i.getOwnerId()) && matches(ids, i.getImageId()))
              is.add(i);
          }
          images = is;
        }

        return new ServiceResult<DescribeImagesResult>(
          sr.startTime,
          new DescribeImagesResult().withImages(images)
        );
      });
    });
  }

  public Observable<PaginatedServiceResult<DescribeInstancesResult>> describeInstances() {
    return describeInstances(new DescribeInstancesRequest());
  }

  public Observable<PaginatedServiceResult<DescribeInstancesResult>> describeInstances(
    final DescribeInstancesRequest request
  ) {
    return Observable.defer(() -> {
      validateEmpty("Filter", request.getFilters());

      TypeReference<Reservation> ref = new TypeReference<Reservation>() {};
      String url = config.url() + "/api/v2/aws/instances;_expand";
      return doGetList(ref, url)
      .map(sr -> {
        List<Reservation> reservations = sr.result;

        List<String> ids = request.getInstanceIds();
        if (shouldFilter(ids)) {
          List<Reservation> rs = new ArrayList<Reservation>();
          for (Reservation r : reservations) {
            List<Instance> is = new ArrayList<Instance>();
            for (Instance i : r.getInstances()) {
              if (matches(ids, i.getInstanceId()))
                is.add(i);
            }
            if (is.size() > 0)
              rs.add(r.withInstances(is));
          }
          reservations = rs;
        }
        return new PaginatedServiceResult<DescribeInstancesResult>(
          sr.startTime,
          null,
          new DescribeInstancesResult().withReservations(reservations)
        );
      });
    });
  }

  public Observable<PaginatedServiceResult<DescribeReservedInstancesOfferingsResult>> describeReservedInstancesOfferings() {
    return describeReservedInstancesOfferings(new DescribeReservedInstancesOfferingsRequest());
  }

  public Observable<PaginatedServiceResult<DescribeReservedInstancesOfferingsResult>> describeReservedInstancesOfferings(
    final DescribeReservedInstancesOfferingsRequest request
  ) {
    return Observable.defer(() -> {
      validateEmpty("Filter", request.getFilters());
      validateEmpty("AvailabilityZone", request.getAvailabilityZone());
      validateEmpty("IncludeMarketplace", request.getIncludeMarketplace());
      validateEmpty("InstanceTenancy", request.getInstanceTenancy());
      validateEmpty("InstanceType", request.getInstanceType());
      validateEmpty("OfferingType", request.getOfferingType());
      validateEmpty("ProductDescription", request.getProductDescription());

      TypeReference<ReservedInstancesOffering> ref = new TypeReference<ReservedInstancesOffering>() {};
      String url = config.url() + "/api/v2/aws/reservedInstancesOfferings;_expand";
      return doGetList(ref, url)
      .map(sr -> {
        List<ReservedInstancesOffering> reservedInstancesOfferings = sr.result;

        List<String> ids = request.getReservedInstancesOfferingIds();
        if (shouldFilter(ids)) {
          List<ReservedInstancesOffering> rs = new ArrayList<ReservedInstancesOffering>();
          for (ReservedInstancesOffering r : reservedInstancesOfferings) {
            if (matches(ids, r.getReservedInstancesOfferingId()))
              rs.add(r);
          }
          reservedInstancesOfferings = rs;
        }
        return new PaginatedServiceResult<DescribeReservedInstancesOfferingsResult>(
          sr.startTime,
          null,
          new DescribeReservedInstancesOfferingsResult().withReservedInstancesOfferings(reservedInstancesOfferings)
        );
      });
    });
  }

  public Observable<ServiceResult<DescribeSecurityGroupsResult>> describeSecurityGroups() {
    return describeSecurityGroups(new DescribeSecurityGroupsRequest());
  }

  public Observable<ServiceResult<DescribeSecurityGroupsResult>> describeSecurityGroups(
    final DescribeSecurityGroupsRequest request
  ) {
    return Observable.defer(() -> {
      validateEmpty("Filter", request.getFilters());

      TypeReference<SecurityGroup> ref = new TypeReference<SecurityGroup>() {};
      String url = config.url() + "/api/v2/aws/securityGroups;_expand";
      return doGetList(ref, url)
      .map(sr -> {
        List<SecurityGroup> securityGroups = sr.result;

        List<String> names = request.getGroupNames();
        List<String> ids = request.getGroupIds();
        if (shouldFilter(names) || shouldFilter(ids)) {
          List<SecurityGroup> sgs = new ArrayList<SecurityGroup>();
          for (SecurityGroup sg : securityGroups) {
            if (matches(names, sg.getGroupName()) && matches(ids, sg.getGroupId()))
              sgs.add(sg);
          }
          securityGroups = sgs;
        }

        return new ServiceResult<DescribeSecurityGroupsResult>(
          sr.startTime,
          new DescribeSecurityGroupsResult().withSecurityGroups(securityGroups)
        );
      });
    });
  }

  public Observable<ServiceResult<DescribeSubnetsResult>> describeSubnets() {
    return describeSubnets(new DescribeSubnetsRequest());
  }

  public Observable<ServiceResult<DescribeSubnetsResult>> describeSubnets(
    final DescribeSubnetsRequest request
  ) {
    return Observable.defer(() -> {
    validateEmpty("Filter", request.getFilters());

    TypeReference<Subnet> ref = new TypeReference<Subnet>() {};
    String url = config.url() + "/api/v2/aws/subnets;_expand";
      return doGetList(ref, url)
      .map(sr -> {
        List<Subnet> subnets = sr.result;

        List<String> ids = request.getSubnetIds();
        if (shouldFilter(ids)) {
          List<Subnet> ss = new ArrayList<Subnet>();
          for (Subnet s : subnets) {
            if (matches(ids, s.getSubnetId()))
              ss.add(s);
          }
          subnets = ss;
        }

        return new ServiceResult<DescribeSubnetsResult>(
          sr.startTime,
          new DescribeSubnetsResult().withSubnets(subnets)
        );
      });
    });
  }

  public Observable<PaginatedServiceResult<DescribeVolumesResult>> describeVolumes() {
    return describeVolumes(new DescribeVolumesRequest());
  }

  public Observable<PaginatedServiceResult<DescribeVolumesResult>> describeVolumes(
    final DescribeVolumesRequest request
  ) {
    return Observable.defer(() -> {
    validateEmpty("Filter", request.getFilters());

    TypeReference<Volume> ref = new TypeReference<Volume>() {};
    String url = config.url() + "/api/v2/aws/volumes;_expand";
      return doGetList(ref, url)
      .map(sr -> {
        List<Volume> volumes = sr.result;

        List<String> ids = request.getVolumeIds();
        if (shouldFilter(ids)) {
          List<Volume> vs = new ArrayList<Volume>();
          for (Volume v : volumes) {
            if (matches(ids, v.getVolumeId()))
              vs.add(v);
          }
          volumes = vs;
        }

        return new PaginatedServiceResult<DescribeVolumesResult>(
          sr.startTime,
          null,
          new DescribeVolumesResult().withVolumes(volumes)
        );
      });
    });
  }

  public Observable<ServiceResult<DescribeVpcsResult>> describeVpcs() {
    return describeVpcs(new DescribeVpcsRequest());
  }

  public Observable<ServiceResult<DescribeVpcsResult>> describeVpcs(
    final DescribeVpcsRequest request
  ) {
    return Observable.defer(() -> {
    validateEmpty("Filter", request.getFilters());

    TypeReference<Vpc> ref = new TypeReference<Vpc>() {};
    String url = config.url() + "/api/v2/aws/vpcs;_expand";
      return doGetList(ref, url)
      .map(sr -> {
        List<Vpc> vpcs = sr.result;

        List<String> ids = request.getVpcIds();
        if (shouldFilter(ids)) {
          List<Vpc> vs = new ArrayList<Vpc>();
          for (Vpc v : vpcs) {
            if (matches(ids, v.getVpcId()))
              vs.add(v);
          }
          vpcs = vs;
        }

        return new ServiceResult<DescribeVpcsResult>(
          sr.startTime,
          new DescribeVpcsResult().withVpcs(vpcs)
        );
      });
    });
  }

  public Observable<ServiceResult<DescribeVpcClassicLinkResult>> describeVpcClassicLink() {
    return describeVpcClassicLink(new DescribeVpcClassicLinkRequest());
  }

  public Observable<ServiceResult<DescribeVpcClassicLinkResult>> describeVpcClassicLink(
    final DescribeVpcClassicLinkRequest request
  ) {
    return Observable.defer(() -> {
    validateEmpty("Filter", request.getFilters());

    TypeReference<VpcClassicLink> ref = new TypeReference<VpcClassicLink>() {};
    String url = config.url() + "/api/v2/aws/vpcClassicLinks;_expand";
      return doGetList(ref, url)
      .map(sr -> {
        List<VpcClassicLink> vpcs = sr.result;

        List<String> ids = request.getVpcIds();
        if (shouldFilter(ids)) {
          List<VpcClassicLink> vs = new ArrayList<VpcClassicLink>();
          for (VpcClassicLink v : vpcs) {
            if (matches(ids, v.getVpcId()))
              vs.add(v);
          }
          vpcs = vs;
        }

        return new ServiceResult<DescribeVpcClassicLinkResult>(
          sr.startTime,
          new DescribeVpcClassicLinkResult().withVpcs(vpcs)
        );
      });
    });
  }
}
