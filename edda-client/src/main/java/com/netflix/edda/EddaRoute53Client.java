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

import com.amazonaws.handlers.RequestHandler2;
import com.fasterxml.jackson.core.type.TypeReference;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.*;

public class EddaRoute53Client extends EddaAwsClient {
  public EddaRoute53Client(AwsConfiguration config, String vip, String region) {
    super(config, vip, region, null);
  }

  public EddaRoute53Client(AwsConfiguration config, String vip, String region, RequestHandler2 responseCallback) {
    super(config, vip, region, responseCallback);
  }

  public AmazonRoute53 readOnly() {
    return readOnly(AmazonRoute53.class);
  }

  public AmazonRoute53 wrapAwsClient(AmazonRoute53 delegate) {
    return wrapAwsClient(AmazonRoute53.class, delegate);
  }

  public ListHostedZonesResult listHostedZones() {
    return listHostedZones(new ListHostedZonesRequest());
  }

  public ListHostedZonesResult listHostedZones(ListHostedZonesRequest request) {
    TypeReference<List<HostedZone>> ref = new TypeReference<List<HostedZone>>() {};
    EddaRequest<ListHostedZonesRequest> req = buildRequest(request, "/api/v2/aws/hostedZones;_expand");
    EddaResponse response = doGet(req);
    try {
      List<HostedZone> hostedZones = parse(ref, response);
      return handleResponse(response, req, new ListHostedZonesResult()
        .withHostedZones(hostedZones));
    }
    catch (IOException e) {
      response.setException(new AmazonClientException("Failed to parse " + req.getUrl(), e));
      throw notifyException(req, response);
    }
  }

  public ListResourceRecordSetsResult listResourceRecordSets(ListResourceRecordSetsRequest request) {
    validateNotEmpty("HostedZoneId", request.getHostedZoneId());

    TypeReference<List<ResourceRecordSet>> ref = new TypeReference<List<ResourceRecordSet>>() {};
    String hostedZoneId = request.getHostedZoneId();
    EddaRequest<ListResourceRecordSetsRequest> req = buildRequest(request, "/api/v2/aws/hostedRecords;_expand;zone.id=" + hostedZoneId);
    EddaResponse response = doGet(req);
    try {
      List<ResourceRecordSet> resourceRecordSets = parse(ref, response);
      return handleResponse(response, req, new ListResourceRecordSetsResult()
        .withResourceRecordSets(resourceRecordSets));
    }
    catch (IOException e) {
      response.setException(new AmazonClientException("Failed to parse " + req.getUrl(), e));
      throw notifyException(req, response);
    }
  }
}
