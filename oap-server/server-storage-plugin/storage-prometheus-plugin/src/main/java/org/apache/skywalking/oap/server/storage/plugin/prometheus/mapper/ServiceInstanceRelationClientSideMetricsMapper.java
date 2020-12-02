package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.relation.endpoint.EndpointRelationServerSideMetrics;
import org.apache.skywalking.oap.server.core.analysis.manual.relation.instance.ServiceInstanceRelationClientSideMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Counter;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(ServiceInstanceRelationClientSideMetrics.class)
public class ServiceInstanceRelationClientSideMetricsMapper extends PrometheusMeterMapper<ServiceInstanceRelationClientSideMetrics, Counter> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, ServiceInstanceRelationClientSideMetrics metrics) {
		try {
			Map<String, String> labels = PrometheusMeterMapper.extractMetricsColumnValues(model, metrics);
			
			return new MetricFamilySamples(model.getName(), Type.COUNTER, "", 
					Collections.singletonList(
							new Sample(
									model.getName(), 
									new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
									0, 
									TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling())
									)));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public ServiceInstanceRelationClientSideMetrics prometheusToSkywalking(Model model, Counter metric) {
		try {
			ServiceInstanceRelationClientSideMetrics metrics = (ServiceInstanceRelationClientSideMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
			metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			metrics.setComponentId(Integer.parseInt(metric.getLabels().get(EndpointRelationServerSideMetrics.COMPONENT_ID)));
			metrics.setEntityId(metric.getLabels().get(EndpointRelationServerSideMetrics.ENTITY_ID));
			metrics.setDestEndpoint(metric.getLabels().get(EndpointRelationServerSideMetrics.DEST_ENDPOINT));
			metrics.setSourceEndpoint(metric.getLabels().get(EndpointRelationServerSideMetrics.SOURCE_ENDPOINT));
			return metrics;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}

}
