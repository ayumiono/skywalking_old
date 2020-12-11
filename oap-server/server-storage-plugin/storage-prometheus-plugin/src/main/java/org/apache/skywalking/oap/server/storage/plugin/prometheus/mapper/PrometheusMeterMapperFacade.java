package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PromeMetric;

import io.prometheus.client.Collector.MetricFamilySamples;
import lombok.Getter;
import lombok.Singular;

@Getter
public class PrometheusMeterMapperFacade extends PrometheusMeterMapper<Metrics, PromeMetric>{
	
	Map<Class<?>, PrometheusMeterMapper<Metrics, PromeMetric>> delegates = new HashMap<>();
	
	@lombok.Builder
    public PrometheusMeterMapperFacade(@Singular("delegate") Map<Class<?>, PrometheusMeterMapper<Metrics, PromeMetric>> delegates) {
		this.delegates = delegates;
	}

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, Metrics metrics, int age) {
		if(delegates.containsKey(metrics.getClass())) {
			MetricFamilySamples mfs = delegates.get(metrics.getClass()).skywalkingToPrometheus(model, metrics, age);
			if(mfs == null || mfs.samples == null || mfs.samples.size() == 0) {
				throw new RuntimeException(model.getName() + " : " + metrics.getClass().getSimpleName() + " null MetricFamilySamples");
			}
			return mfs;
		}else if(delegates.containsKey(metrics.getClass().getSuperclass())){
			MetricFamilySamples mfs = delegates.get(metrics.getClass().getSuperclass()).skywalkingToPrometheus(model, metrics, age);
			if(mfs == null || mfs.samples == null || mfs.samples.size() == 0) {
				throw new RuntimeException(model.getName() + " : " + metrics.getClass().getSimpleName() + " null MetricFamilySamples");
			}
			return mfs;
		}else {
			throw new RuntimeException("cannot find matched PrometheusMeterMapper for " + metrics.getClass().getName() + " super class " + metrics.getClass().getSuperclass());
		}
	}

	@Override
	public Metrics prometheusToSkywalking(Model model, List<PromeMetric> metricList) {
		if(delegates.containsKey(model.getStorageModelClazz())) {
			Metrics metrics = delegates.get(model.getStorageModelClazz()).prometheusToSkywalking(model, metricList);
			if(metrics == null) {
				throw new RuntimeException(model.getName() + " : " + StringUtils.join(metricList, ";") + " null metrics");
			}
			return metrics;
		}else if(delegates.containsKey(model.getStorageModelClazz().getSuperclass())){
			Metrics metrics = delegates.get(model.getStorageModelClazz().getSuperclass()).prometheusToSkywalking(model, metricList);
			if(metrics == null) {
				throw new RuntimeException(model.getName() + " : " + StringUtils.join(metricList, ";") + " null metrics");
			}
			return metrics;
		}else {
			throw new RuntimeException("cannot find matched PrometheusMeterMapper for " + model.getStorageModelClazz().getName()  + " super class " + model.getStorageModelClazz().getSuperclass());
		}
	}
}
