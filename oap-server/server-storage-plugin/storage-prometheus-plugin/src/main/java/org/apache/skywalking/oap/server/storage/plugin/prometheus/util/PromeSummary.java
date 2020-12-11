package org.apache.skywalking.oap.server.storage.plugin.prometheus.util;

import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class PromeSummary extends PromeMetric {

    private long sampleCount;
    private double sampleSum;
    
    private int precision;
    private final Map<String, Long> quantiles;
    
    private final Map<String, Long> dataset;

    @lombok.Builder
    public PromeSummary(String name, @Singular Map<String, String> labels, long sampleCount, double sampleSum,
        @Singular("quantile") Map<String, Long> quantiles, long timestamp, int precision, 
        @Singular("dataset") Map<String, Long> dataset, int age, String id) {
        super(name, labels, timestamp, age, id);
        getLabels().remove("quantile");
        this.sampleCount = sampleCount;
        this.sampleSum = sampleSum;
        this.quantiles = quantiles;
        this.dataset = dataset;
        this.precision = precision;
    }

    @Override public PromeMetric sum(PromeMetric m) {
    	PromeSummary s = (PromeSummary) m;
        this.sampleCount =  this.sampleCount + s.getSampleCount();
        this.sampleSum = this.sampleSum + s.getSampleSum();
        return this;
    }

    @Override public Double value() {
        return this.getSampleSum() * 1000 / this.getSampleCount();
    }
}
