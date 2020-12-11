/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.storage.plugin.prometheus.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.skywalking.oap.server.library.util.prometheus.metrics.MetricType;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Contains all metrics within a family (that is, of the same name). All metrics in a family have the same type.
 */
@EqualsAndHashCode
@ToString
public class PromeMetricFamily {

    public static class Builder {
        private String name;
        private String help;
        private MetricType type;
        private List<PromeMetric> metrics;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setHelp(String help) {
            this.help = help;
            return this;
        }

        public Builder setType(MetricType type) {
            this.type = type;
            return this;
        }

        public Builder addMetric(PromeMetric metric) {
            if (metrics == null) {
                metrics = new ArrayList<>();
            }
            metrics.add(metric);
            return this;
        }

        public PromeMetricFamily build() {
            return new PromeMetricFamily(this);
        }
    }

    private final String name;
    private final String help;
    private final MetricType type;
    private final List<PromeMetric> metrics;

    protected PromeMetricFamily(Builder builder) {
        if (builder.name == null) {
            throw new IllegalArgumentException("Need to set name");
        }
        if (builder.type == null) {
            throw new IllegalArgumentException("Need to set type");
        }

        Class<? extends PromeMetric> expectedMetricClassType;
        switch (builder.type) {
            case COUNTER:
                expectedMetricClassType = PromeCounter.class;
                break;
            case GAUGE:
                expectedMetricClassType = PromeGauge.class;
                break;
            case SUMMARY:
                expectedMetricClassType = PromeSummary.class;
                break;
            case HISTOGRAM:
                expectedMetricClassType = PromeHistogram.class;
                break;
            default:
                throw new IllegalArgumentException("Invalid type: " + builder.type);
        }

        // make sure all the metrics in the family are of the expected type
        if (builder.metrics != null && !builder.metrics.isEmpty()) {
            for (PromeMetric metric : builder.metrics) {
                if (!expectedMetricClassType.isInstance(metric)) {
                    throw new IllegalArgumentException(
                        String.format("Metric type is [%s] so instances of class [%s] are expected, "
                                + "but got metric object of type [%s]",
                            builder.type, expectedMetricClassType.getName(), metric.getClass().getName()));
                }
            }

        }

        this.name = builder.name;
        this.help = builder.help;
        this.type = builder.type;
        this.metrics = builder.metrics;
    }

    public String getName() {
        return name;
    }

    public String getHelp() {
        return help;
    }

    public MetricType getType() {
        return type;
    }

    public List<PromeMetric> getMetrics() {
        if (metrics == null) {
            return Collections.emptyList();
        }
        return metrics;
    }
}

