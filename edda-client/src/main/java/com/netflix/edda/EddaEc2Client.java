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
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;

public class EddaEc2Client extends EddaAwsClient {
  public EddaEc2Client(AwsConfiguration config) {
    super(config);
  }

  public EddaEc2Client(AwsConfiguration config, ResponseCallback responseCallback) {
    super(config, responseCallback);
  }

  public AmazonEC2 readOnly() {
    return readOnly(AmazonEC2.class);
  }

  public AmazonEC2 wrapAwsClient(AmazonEC2 delegate) {
    return wrapAwsClient(AmazonEC2.class, delegate);
  }

  public DescribeClassicLinkInstancesResult describeClassicLinkInstances() {
    return describeClassicLinkInstances(new DescribeClassicLinkInstancesRequest());
  }

  public DescribeClassicLinkInstancesResult describeClassicLinkInstances(DescribeClassicLinkInstancesRequest request) {
    validateEmpty("Filter", request.getFilters());

    TypeReference<List<ClassicLinkInstance>> ref = new TypeReference<List<ClassicLinkInstance>>() {};
    String url = config.url() + "/api/v2/aws/classicLinkInstances;_expand";
    try {
      List<ClassicLinkInstance> instances = parse(ref, doGet(url));

      List<String> ids = request.getInstanceIds();
      if (shouldFilter(ids)) {
        List<ClassicLinkInstance> is = new ArrayList<ClassicLinkInstance>();
        for (ClassicLinkInstance i : instances) {
          if (matches(ids, i.getInstanceId()))
            is.add(i);
        }
        instances = is;
      }

      return new DescribeClassicLinkInstancesResult()
        .withInstances(instances);
    }
    catch (IOException e) {
      throw new AmazonClientException("Failed to parse " + url, e);
    }
  }

  public DescribeImagesResult describeImages() {
    return describeImages(new DescribeImagesRequest());
  }

  public DescribeImagesResult describeImages(DescribeImagesRequest request) {
    validateEmpty("ExecutableUsers", request.getExecutableUsers());
    validateEmpty("Filter", request.getFilters());

    TypeReference<List<Image>> ref = new TypeReference<List<Image>>() {};
    String url = config.url() + "/api/v2/aws/images;_expand";
    try {
      List<Image> images = parse(ref, doGet(url));

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

      return new DescribeImagesResult()
        .withImages(images);
    }
    catch (IOException e) {
      throw new AmazonClientException("Failed to parse " + url, e);
    }
  }

  public DescribeInstancesResult describeInstances() {
    return describeInstances(new DescribeInstancesRequest());
  }

  public DescribeInstancesResult describeInstances(DescribeInstancesRequest request) {
    validateEmpty("Filter", request.getFilters());

    TypeReference<List<Reservation>> ref = new TypeReference<List<Reservation>>() {};
    String url = config.url() + "/api/v2/aws/instances;_expand";
    try {
      List<Reservation> reservations = parse(ref, doGet(url));

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
      return new DescribeInstancesResult()
        .withReservations(reservations);
    }
    catch (IOException e) {
      throw new AmazonClientException("Failed to parse " + url, e);
    }
  }

  public DescribeInstanceStatusResult describeInstanceStatus() {
    return describeInstanceStatus(new DescribeInstanceStatusRequest());
  }

  public DescribeInstanceStatusResult describeInstanceStatus(DescribeInstanceStatusRequest request) {
    validateEmpty("Filter", request.getFilters());

    TypeReference<List<InstanceStatus>> ref = new TypeReference<List<InstanceStatus>>() {};
    String url = config.url() + "/api/v2/aws/instanceStatuses;_expand";
    try {
      List<InstanceStatus> statuses = parse(ref, doGet(url));

      List<String> ids = request.getInstanceIds();
      if (shouldFilter(ids)) {
        List<InstanceStatus> rs = new ArrayList<>();
        for (InstanceStatus is : statuses) {
          if (matches(ids, is.getInstanceId())) {
            rs.add(is);
          }
        }
        statuses = rs;
      }
      return new DescribeInstanceStatusResult().withInstanceStatuses(statuses);
    }
    catch (IOException e) {
      throw new AmazonClientException("Failed to parse " + url, e);
    }
  }

  public DescribeReservedInstancesOfferingsResult describeReservedInstancesOfferings() {
    return describeReservedInstancesOfferings(new DescribeReservedInstancesOfferingsRequest());
  }

  public DescribeReservedInstancesOfferingsResult describeReservedInstancesOfferings(DescribeReservedInstancesOfferingsRequest request) {
    validateEmpty("Filter", request.getFilters());
    validateEmpty("AvailabilityZone", request.getAvailabilityZone());
    validateEmpty("IncludeMarketplace", request.getIncludeMarketplace());
    validateEmpty("InstanceTenancy", request.getInstanceTenancy());
    validateEmpty("InstanceType", request.getInstanceType());
    validateEmpty("OfferingType", request.getOfferingType());
    validateEmpty("ProductDescription", request.getProductDescription());

    TypeReference<List<ReservedInstancesOffering>> ref = new TypeReference<List<ReservedInstancesOffering>>() {};
    String url = config.url() + "/api/v2/aws/reservedInstancesOfferings;_expand";
    try {
      List<ReservedInstancesOffering> reservedInstancesOfferings = parse(ref, doGet(url));

      List<String> ids = request.getReservedInstancesOfferingIds();
      if (shouldFilter(ids)) {
        List<ReservedInstancesOffering> rs = new ArrayList<ReservedInstancesOffering>();
        for (ReservedInstancesOffering r : reservedInstancesOfferings) {
          if (matches(ids, r.getReservedInstancesOfferingId()))
            rs.add(r);
        }
        reservedInstancesOfferings = rs;
      }
      return new DescribeReservedInstancesOfferingsResult()
        .withReservedInstancesOfferings(reservedInstancesOfferings);
    }
    catch (IOException e) {
      throw new AmazonClientException("Failed to parse " + url, e);
    }
  }

  public DescribeSecurityGroupsResult describeSecurityGroups() {
    return describeSecurityGroups(new DescribeSecurityGroupsRequest());
  }

  public DescribeSecurityGroupsResult describeSecurityGroups(DescribeSecurityGroupsRequest request) {
    validateEmpty("Filter", request.getFilters());

    TypeReference<List<SecurityGroup>> ref = new TypeReference<List<SecurityGroup>>() {};
    String url = config.url() + "/api/v2/aws/securityGroups;_expand";
    try {
      List<SecurityGroup> securityGroups = parse(ref, doGet(url));

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

      return new DescribeSecurityGroupsResult()
        .withSecurityGroups(securityGroups);
    }
    catch (IOException e) {
      throw new AmazonClientException("Failed to parse " + url, e);
    }
  }

  public DescribeSubnetsResult describeSubnets() {
    return describeSubnets(new DescribeSubnetsRequest());
  }

  public DescribeSubnetsResult describeSubnets(DescribeSubnetsRequest request) {
    validateEmpty("Filter", request.getFilters());

    TypeReference<List<Subnet>> ref = new TypeReference<List<Subnet>>() {};
    String url = config.url() + "/api/v2/aws/subnets;_expand";
    try {
      List<Subnet> subnets = parse(ref, doGet(url));

      List<String> ids = request.getSubnetIds();
      if (shouldFilter(ids)) {
        List<Subnet> ss = new ArrayList<Subnet>();
        for (Subnet s : subnets) {
          if (matches(ids, s.getSubnetId()))
            ss.add(s);
        }
        subnets = ss;
      }

      return new DescribeSubnetsResult()
        .withSubnets(subnets);
    }
    catch (IOException e) {
      throw new AmazonClientException("Failed to parse " + url, e);
    }
  }

  public DescribeVolumesResult describeVolumes() {
    return describeVolumes(new DescribeVolumesRequest());
  }

  public DescribeVolumesResult describeVolumes(DescribeVolumesRequest request) {
    validateEmpty("Filter", request.getFilters());

    TypeReference<List<Volume>> ref = new TypeReference<List<Volume>>() {};
    String url = config.url() + "/api/v2/aws/volumes;_expand";
    try {
      List<Volume> volumes = parse(ref, doGet(url));

      List<String> ids = request.getVolumeIds();
      if (shouldFilter(ids)) {
        List<Volume> vs = new ArrayList<Volume>();
        for (Volume v : volumes) {
          if (matches(ids, v.getVolumeId()))
            vs.add(v);
        }
        volumes = vs;
      }

      return new DescribeVolumesResult()
        .withVolumes(volumes);
    }
    catch (IOException e) {
      throw new AmazonClientException("Failed to parse " + url, e);
    }
  }

  public DescribeVpcsResult describeVpcs() {
    return describeVpcs(new DescribeVpcsRequest());
  }

  public DescribeVpcsResult describeVpcs(DescribeVpcsRequest request) {
    validateEmpty("Filter", request.getFilters());

    TypeReference<List<Vpc>> ref = new TypeReference<List<Vpc>>() {};
    String url = config.url() + "/api/v2/aws/vpcs;_expand";
    try {
      List<Vpc> vpcs = parse(ref, doGet(url));

      List<String> ids = request.getVpcIds();
      if (shouldFilter(ids)) {
        List<Vpc> vs = new ArrayList<Vpc>();
        for (Vpc v : vpcs) {
          if (matches(ids, v.getVpcId()))
            vs.add(v);
        }
        vpcs = vs;
      }

      return new DescribeVpcsResult()
        .withVpcs(vpcs);
    }
    catch (IOException e) {
      throw new AmazonClientException("Failed to parse " + url, e);
    }
  }
}
