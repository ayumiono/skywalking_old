package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.HashMap;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Metric;

import io.prometheus.client.Collector.MetricFamilySamples;
import lombok.Getter;
import lombok.Singular;

@Getter
public class PrometheusMeterMapperFacade extends PrometheusMeterMapper<Metrics, Metric>{
	
	Map<Class<?>, PrometheusMeterMapper<Metrics, Metric>> delegates = new HashMap<>();
	
	@lombok.Builder
    public PrometheusMeterMapperFacade(@Singular("delegate") Map<Class<?>, PrometheusMeterMapper<Metrics, Metric>> delegates) {
		this.delegates = delegates;
	}

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, Metrics metrics) {
		return delegates.get(metrics.getClass()).skywalkingToPrometheus(model, metrics);
	}

	@Override
	public Metrics prometheusToSkywalking(Model model, Metric metric) {
		return delegates.get(model.getStorageModelClazz()).prometheusToSkywalking(model, metric);
	}
}
