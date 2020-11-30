package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import org.apache.skywalking.oap.server.core.analysis.manual.relation.service.ServiceRelationClientSideMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Counter;

import io.prometheus.client.Collector.MetricFamilySamples;

@PrometheusMetricsMapper(ServiceRelationClientSideMetrics.class)
public class ServiceRelationClientSideMetricsMapper extends PrometheusMeterMapper<ServiceRelationClientSideMetrics, Counter> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, ServiceRelationClientSideMetrics metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceRelationClientSideMetrics prometheusToSkywalking(Model model, Counter metric) {
		// TODO Auto-generated method stub
		return null;
	}

}
