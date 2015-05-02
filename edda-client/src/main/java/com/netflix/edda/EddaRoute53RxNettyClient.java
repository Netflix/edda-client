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
import com.amazonaws.services.route53.AmazonRoute53RxNetty;
import com.amazonaws.services.route53.model.*;

import com.amazonaws.services.ServiceResult;
import com.amazonaws.services.PaginatedServiceResult;

import rx.Observable;

public class EddaRoute53RxNettyClient extends EddaAwsRxNettyClient {
  public EddaRoute53RxNettyClient(AwsConfiguration config, String vip, String region) {
    super(config, vip, region);
  }

  public AmazonRoute53RxNetty readOnly() {
    return readOnly(AmazonRoute53RxNetty.class);
  }

  public AmazonRoute53RxNetty wrapAwsClient(AmazonRoute53RxNetty delegate) {
    return wrapAwsClient(AmazonRoute53RxNetty.class, delegate);
  }

  public Observable<PaginatedServiceResult<ListHostedZonesResult>> listHostedZones() {
    return listHostedZones(new ListHostedZonesRequest());
  }

  public Observable<PaginatedServiceResult<ListHostedZonesResult>> listHostedZones(
    final ListHostedZonesRequest request
  ) {
    return Observable.defer(() -> {
      TypeReference<List<HostedZone>> ref = new TypeReference<List<HostedZone>>() {};
      String url = config.url() + "/api/v2/aws/hostedZones;_expand";
      return doGet(url).map(sr -> {
        try {
          List<HostedZone> hostedZones = parse(ref, sr.result);
          return new PaginatedServiceResult<ListHostedZonesResult>(
            sr.startTime,
            null,
            new ListHostedZonesResult().withHostedZones(hostedZones)
          );
        }
        catch (IOException e) {
          throw new AmazonClientException("Faled to parse " + url, e);
        }
      });
    });
  }

  public Observable<PaginatedServiceResult<ListResourceRecordSetsResult>> listResourceRecordSets() {
    return Observable.defer(() -> {
      TypeReference<List<ResourceRecordSet>> ref = new TypeReference<List<ResourceRecordSet>>() {};

      String url = config.url() + "/api/v2/aws/hostedRecords;_expand";
      return doGet(url).map(sr -> {
        try {
          List<ResourceRecordSet> resourceRecordSets = parse(ref, sr.result);
          return new PaginatedServiceResult<ListResourceRecordSetsResult>(
            sr.startTime,
            null,
            new ListResourceRecordSetsResult().withResourceRecordSets(resourceRecordSets)
          );
        }
        catch (IOException e) {
          throw new AmazonClientException("Faled to parse " + url, e);
        }
      });
    });
  }

  public Observable<PaginatedServiceResult<ListResourceRecordSetsResult>> listResourceRecordSets(
    final ListResourceRecordSetsRequest request
  ) {
    return Observable.defer(() -> {
      validateNotEmpty("HostedZoneId", request.getHostedZoneId());

      TypeReference<List<ResourceRecordSet>> ref = new TypeReference<List<ResourceRecordSet>>() {};
      String hostedZoneId = request.getHostedZoneId();

      String url = config.url() + "/api/v2/aws/hostedRecords;_expand;zone.id=" + hostedZoneId;
      return doGet(url).map(sr -> {
        try {
          List<ResourceRecordSet> resourceRecordSets = parse(ref, sr.result);
          return new PaginatedServiceResult<ListResourceRecordSetsResult>(
            sr.startTime,
            null,
            new ListResourceRecordSetsResult().withResourceRecordSets(resourceRecordSets)
          );
        }
        catch (IOException e) {
          throw new AmazonClientException("Faled to parse " + url, e);
        }
      });
    });
  }
}
