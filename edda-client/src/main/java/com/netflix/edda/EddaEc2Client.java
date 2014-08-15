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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.*;

import com.netflix.ie.ipc.Http$;
import com.netflix.ie.ipc.HttpResponse;

public class EddaEc2Client {
  private final AwsConfiguration config;

  public EddaEc2Client(AwsConfiguration config) {
    this.config = config;
  }

  private HttpResponse doGet(String uri) {
    HttpResponse res = Http$.get(uri);
    if (res.status() != 200) {
      AmazonServiceException e = new AmazonServiceException("Failed to fetch " + uri);
      e.setStatusCode(res.status());
      e.setErrorCode("Edda");
      e.setRequestId(uri);
      throw e;
    }
    return res;
  }

  private <T> T parse(TypeReference<T> ref, byte[] body) throws IOException {
      return JsonHelper$.createParser(new ByteArrayInputStream(body)).readValueAs(ref);
  }

  public DescribeImagesResult describeImages() {
    return describeImages(new DescribeImagesRequest());
  }

  public DescribeImagesResult describeImages(DescribeImagesRequest request) {
    TypeReference<List<Image>> ref = new TypeReference<List<Image>>() {};
    String url = config.url() + "/api/v2/aws/images;_expand";
    try {
      List<Image> images = parse(ref, doGet(url).body());
      return new DescribeImagesResult()
        .withImages(images);
    }
    catch (IOException e) {
      throw new AmazonClientException("Faled to parse " + url, e);
    }
  }

  public DescribeInstancesResult describeInstances() {
    return describeInstances(new DescribeInstancesRequest());
  }

  public DescribeInstancesResult describeInstances(DescribeInstancesRequest request) {
    TypeReference<List<Reservation>> ref = new TypeReference<List<Reservation>>() {};
    String url = config.url() + "/api/v2/aws/instances;_expand";
    try {
      List<Reservation> reservations = parse(ref, doGet(url).body());
      return new DescribeInstancesResult()
        .withReservations(reservations);
    }
    catch (IOException e) {
      throw new AmazonClientException("Faled to parse " + url, e);
    }
  }

  public DescribeSecurityGroupsResult describeSecurityGroups() {
    return describeSecurityGroups(new DescribeSecurityGroupsRequest());
  }

  public DescribeSecurityGroupsResult describeSecurityGroups(DescribeSecurityGroupsRequest request) {
    TypeReference<List<SecurityGroup>> ref = new TypeReference<List<SecurityGroup>>() {};
    String url = config.url() + "/api/v2/aws/securityGroups;_expand";
    try {
      List<SecurityGroup> securityGroups = parse(ref, doGet(url).body());
      return new DescribeSecurityGroupsResult()
        .withSecurityGroups(securityGroups);
    }
    catch (IOException e) {
      throw new AmazonClientException("Faled to parse " + url, e);
    }
  }

  public DescribeSnapshotsResult describeSnapshots() {
    return describeSnapshots(new DescribeSnapshotsRequest());
  }

  public DescribeSnapshotsResult describeSnapshots(DescribeSnapshotsRequest request) {
    TypeReference<List<Snapshot>> ref = new TypeReference<List<Snapshot>>() {};
    String url = config.url() + "/api/v2/aws/snapshots;_expand";
    try {
      List<Snapshot> snapshots = parse(ref, doGet(url).body());
      return new DescribeSnapshotsResult()
        .withSnapshots(snapshots);
    }
    catch (IOException e) {
      throw new AmazonClientException("Faled to parse " + url, e);
    }
  }

  public DescribeSubnetsResult describeSubnets() {
    return describeSubnets(new DescribeSubnetsRequest());
  }

  public DescribeSubnetsResult describeSubnets(DescribeSubnetsRequest request) {
    TypeReference<List<Subnet>> ref = new TypeReference<List<Subnet>>() {};
    String url = config.url() + "/api/v2/aws/subnets;_expand";
    try {
      List<Subnet> subnets = parse(ref, doGet(url).body());
      return new DescribeSubnetsResult()
        .withSubnets(subnets);
    }
    catch (IOException e) {
      throw new AmazonClientException("Faled to parse " + url, e);
    }
  }

  public DescribeVolumesResult describeVolumes() {
    return describeVolumes(new DescribeVolumesRequest());
  }

  public DescribeVolumesResult describeVolumes(DescribeVolumesRequest request) {
    TypeReference<List<Volume>> ref = new TypeReference<List<Volume>>() {};
    String url = config.url() + "/api/v2/aws/volumes;_expand";
    try {
      List<Volume> volumes = parse(ref, doGet(url).body());
      return new DescribeVolumesResult()
        .withVolumes(volumes);
    }
    catch (IOException e) {
      throw new AmazonClientException("Faled to parse " + url, e);
    }
  }
}
