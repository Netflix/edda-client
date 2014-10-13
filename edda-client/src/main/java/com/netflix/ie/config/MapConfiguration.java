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
package com.netflix.ie.config;

import java.util.Collections;
import java.util.Map;

public class MapConfiguration implements IConfiguration {
  private final String prefix;
  private final Map<String,String> props;

  public MapConfiguration(String prefix, Map<String,String> props) {
    this.prefix = prefix;
    this.props = Collections.unmodifiableMap(props);
  }

  public String get(String key) {
    return props.get((prefix == null) ? key : prefix + "." + key);
  }
}
