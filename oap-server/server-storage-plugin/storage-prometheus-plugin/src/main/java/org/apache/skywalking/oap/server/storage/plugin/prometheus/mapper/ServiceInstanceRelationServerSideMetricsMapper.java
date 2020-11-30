package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import org.apache.skywalking.oap.server.core.analysis.manual.relation.instance.ServiceInstanceRelationServerSideMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Counter;

import io.prometheus.client.Collector.MetricFamilySamples;

@PrometheusMetricsMapper(ServiceInstanceRelationServerSideMetrics.class)
public class ServiceInstanceRelationServerSideMetricsMapper extends PrometheusMeterMapper<ServiceInstanceRelationServerSideMetrics, Counter> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, ServiceInstanceRelationServerSideMetrics metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceInstanceRelationServerSideMetrics prometheusToSkywalking(Model model, Counter metric) {
		// TODO Auto-generated method stub
		return null;
	}

}
