package org.apache.skywalking.oap.server.storage.plugin.prometheus.util;

import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class PromeGauge extends PromeMetric {

    private double value;

    @lombok.Builder
    public PromeGauge(String name, @Singular Map<String, String> labels, double value, long timestamp, int age, String id) {
        super(name, labels, timestamp, age, id);
        this.value = value;
    }

    @Override public PromeMetric sum(PromeMetric m) {
        this.value = this.value + m.value();
        return this;
    }

    @Override public Double value() {
        return this.value;
    }
}
