package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import org.apache.skywalking.oap.server.core.analysis.manual.relation.instance.ServiceInstanceRelationClientSideMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Counter;

import io.prometheus.client.Collector.MetricFamilySamples;

@PrometheusMetricsMapper(ServiceInstanceRelationClientSideMetrics.class)
public class ServiceInstanceRelationClientSideMetricsMapper extends PrometheusMeterMapper<ServiceInstanceRelationClientSideMetrics, Counter> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, ServiceInstanceRelationClientSideMetrics metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceInstanceRelationClientSideMetrics prometheusToSkywalking(Model model, Counter metric) {
		// TODO Auto-generated method stub
		return null;
	}

}
