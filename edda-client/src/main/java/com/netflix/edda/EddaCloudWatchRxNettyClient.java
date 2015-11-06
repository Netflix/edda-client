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
import com.amazonaws.services.cloudwatch.AmazonCloudWatchRxNetty;
import com.amazonaws.services.cloudwatch.model.*;

import com.amazonaws.services.ServiceResult;
import com.amazonaws.services.PaginatedServiceResult;

import rx.Observable;

public class EddaCloudWatchRxNettyClient extends EddaAwsRxNettyClient {
  public EddaCloudWatchRxNettyClient(AwsConfiguration config, String vip, String region) {
    super(config, vip, region);
  }

  public AmazonCloudWatchRxNetty readOnly() {
    return readOnly(AmazonCloudWatchRxNetty.class);
  }

  public AmazonCloudWatchRxNetty wrapAwsClient(AmazonCloudWatchRxNetty delegate) {
    return wrapAwsClient(AmazonCloudWatchRxNetty.class, delegate);
  }

  public Observable<PaginatedServiceResult<DescribeAlarmsResult>> describeAlarms() {
    return describeAlarms(new DescribeAlarmsRequest());
  }

  public Observable<PaginatedServiceResult<DescribeAlarmsResult>> describeAlarms(
    final DescribeAlarmsRequest request
  ) {
    return Observable.defer(() -> {
      validateEmpty("ActionPrefix", request.getActionPrefix());
      validateEmpty("AlarmNamePrefix", request.getAlarmNamePrefix());

      TypeReference<MetricAlarm> ref = new TypeReference<MetricAlarm>() {};
      String url = config.url() + "/api/v2/aws/alarms;_expand";
      return doGetList(ref, url)
      .map(sr -> {
        List<MetricAlarm> metricAlarms = sr.result;

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

        return new PaginatedServiceResult<DescribeAlarmsResult>(
          sr.startTime,
          null,
          new DescribeAlarmsResult().withMetricAlarms(metricAlarms)
        );
      });
    });
  }
}
