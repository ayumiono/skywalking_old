package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import org.apache.skywalking.oap.server.core.analysis.manual.relation.service.ServiceRelationServerSideMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Counter;

import io.prometheus.client.Collector.MetricFamilySamples;

@PrometheusMetricsMapper(ServiceRelationServerSideMetrics.class)
public class ServiceRelationServerSideMetricsMapper extends PrometheusMeterMapper<ServiceRelationServerSideMetrics, Counter> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, ServiceRelationServerSideMetrics metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceRelationServerSideMetrics prometheusToSkywalking(Model model, Counter metric) {
		// TODO Auto-generated method stub
		return null;
	}

}
