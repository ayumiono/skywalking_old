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
		if(delegates.containsKey(metrics.getClass())) {
			MetricFamilySamples mfs = delegates.get(metrics.getClass()).skywalkingToPrometheus(model, metrics);
			if(mfs == null || mfs.samples == null || mfs.samples.size() == 0) {
				throw new RuntimeException(model.getName() + " : " + metrics.getClass().getSimpleName() + " null MetricFamilySamples");
			}
			return mfs;
		}else if(delegates.containsKey(metrics.getClass().getSuperclass())){
			MetricFamilySamples mfs = delegates.get(metrics.getClass().getSuperclass()).skywalkingToPrometheus(model, metrics);
			if(mfs == null || mfs.samples == null || mfs.samples.size() == 0) {
				throw new RuntimeException(model.getName() + " : " + metrics.getClass().getSimpleName() + " null MetricFamilySamples");
			}
			return mfs;
		}else {
			throw new RuntimeException("cannot find matched PrometheusMeterMapper for " + metrics.getClass().getName() + " super class " + metrics.getClass().getSuperclass());
		}
	}

	@Override
	public Metrics prometheusToSkywalking(Model model, Metric metric) {
		if(delegates.containsKey(model.getStorageModelClazz())) {
			Metrics metrics = delegates.get(model.getStorageModelClazz()).prometheusToSkywalking(model, metric);
			if(metrics == null) {
				throw new RuntimeException(model.getName() + " : " + metric.toString() + " null metrics");
			}
			return metrics;
		}else if(delegates.containsKey(model.getStorageModelClazz().getSuperclass())){
			Metrics metrics = delegates.get(model.getStorageModelClazz().getSuperclass()).prometheusToSkywalking(model, metric);
			if(metrics == null) {
				throw new RuntimeException(model.getName() + " : " + metric.toString() + " null metrics");
			}
			return metrics;
		}else {
			throw new RuntimeException("cannot find matched PrometheusMeterMapper for " + model.getStorageModelClazz().getName()  + " super class " + model.getStorageModelClazz().getSuperclass());
		}
	}
}
