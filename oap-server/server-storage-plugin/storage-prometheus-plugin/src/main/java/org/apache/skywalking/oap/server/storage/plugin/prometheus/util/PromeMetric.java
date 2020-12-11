package org.apache.skywalking.oap.server.storage.plugin.prometheus.util;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
public abstract class PromeMetric {

    private final String name;
    private final Map<String, String> labels;
    private final long timestamp;
    private final int age;
    private final String id;

    protected PromeMetric(String name, Map<String, String> labels, long timestamp, int age, String id) {
        this.name = name;
        this.labels = Maps.newHashMap(labels);
        this.timestamp = timestamp;
        this.age = age;
        this.id = id;
    }

    public abstract PromeMetric sum(PromeMetric m);

    public abstract Double value();
}
