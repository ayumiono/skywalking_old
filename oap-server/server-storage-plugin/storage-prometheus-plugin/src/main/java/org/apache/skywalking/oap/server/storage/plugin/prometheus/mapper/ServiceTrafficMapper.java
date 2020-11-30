package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import org.apache.skywalking.oap.server.core.analysis.manual.service.ServiceTraffic;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Counter;

import io.prometheus.client.Collector.MetricFamilySamples;

@PrometheusMetricsMapper(ServiceTraffic.class)
public class ServiceTrafficMapper extends PrometheusMeterMapper<ServiceTraffic, Counter> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, ServiceTraffic metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceTraffic prometheusToSkywalking(Model model, Counter metric) {
		// TODO Auto-generated method stub
		return null;
	}

}
