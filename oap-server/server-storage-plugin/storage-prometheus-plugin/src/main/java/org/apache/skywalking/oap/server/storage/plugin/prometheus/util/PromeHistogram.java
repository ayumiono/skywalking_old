package org.apache.skywalking.oap.server.storage.plugin.prometheus.util;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Metric;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class PromeHistogram extends PromeMetric {

    private long sampleCount;
    private double sampleSum;
    private Map<String, Long> buckets;//Map<Double, Long> -> Map<String, Long>

    @lombok.Builder
    public PromeHistogram(String name, @Singular Map<String, String> labels, long sampleCount, double sampleSum,
        @Singular Map<String, Long> buckets, long timestamp, int age, String id) {
        super(name, labels, timestamp, age, id);
        getLabels().remove("le");
        this.sampleCount = sampleCount;
        this.sampleSum = sampleSum;
        this.buckets = buckets;
    }

    @Override public PromeMetric sum(PromeMetric m) {
    	PromeHistogram h = (PromeHistogram) m;
        this.buckets = Stream.concat(getBuckets().entrySet().stream(), h.getBuckets().entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum, TreeMap::new));
        this.sampleSum = this.sampleSum + h.sampleSum;
        this.sampleCount = this.sampleCount + h.sampleCount;
        return this;
    }

    @Override public Double value() {
        return this.getSampleSum() * 1000 / this.getSampleCount();
    }
}
