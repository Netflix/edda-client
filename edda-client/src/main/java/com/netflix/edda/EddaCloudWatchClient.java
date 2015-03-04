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

import com.amazonaws.Request;
import com.amazonaws.handlers.RequestHandler2;
import com.fasterxml.jackson.core.type.TypeReference;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.*;

public class EddaCloudWatchClient extends EddaAwsClient {
  public EddaCloudWatchClient(AwsConfiguration config) {
    super(config);
  }

  public EddaCloudWatchClient(AwsConfiguration config, RequestHandler2 responseCallback) {
    super(config, responseCallback);
  }

  public AmazonCloudWatch readOnly() {
    return readOnly(AmazonCloudWatch.class);
  }

  public AmazonCloudWatch wrapAwsClient(AmazonCloudWatch delegate) {
    return wrapAwsClient(AmazonCloudWatch.class, delegate);
  }

  public DescribeAlarmsResult describeAlarms() {
    return describeAlarms(new DescribeAlarmsRequest());
  }

  public DescribeAlarmsResult describeAlarms(DescribeAlarmsRequest request) {
    validateEmpty("ActionPrefix", request.getActionPrefix());
    validateEmpty("AlarmNamePrefix", request.getAlarmNamePrefix());

    TypeReference<List<MetricAlarm>> ref = new TypeReference<List<MetricAlarm>>() {};
    EddaRequest<DescribeAlarmsRequest> req = buildRequest(request,  "/api/v2/aws/alarms;_expand");
    EddaResponse response = doGet(req);
    try {
      List<MetricAlarm> metricAlarms = parse(ref, response);

      List<String> names = request.getAlarmNames();
      String state = request.getStateValue();
      if (shouldFilter(names) || shouldFilter(state)) {
        List<MetricAlarm> mas = new ArrayList<MetricAlarm>();
        for (MetricAlarm ma : metricAlarms) {
          if (matches(names, ma.getAlarmName()) && matches(state, ma.getStateValue()))
            mas.add(ma);
        }
        metricAlarms = mas;
      }

      return handleResponse(response, req, new DescribeAlarmsResult().withMetricAlarms(metricAlarms));
    }
    catch (IOException e) {
      response.setException(new AmazonClientException("Failed to parse " + req.getUrl(), e));
      throw notifyException(req, response);
    }
  }
}
