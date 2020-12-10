package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.endpoint.EndpointTraffic;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Counter;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(EndpointTraffic.class)
@Deprecated
public class EndpointTrafficMapper extends PrometheusMeterMapper<EndpointTraffic, Counter>{

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, EndpointTraffic metrics, int age) {
		try {
			Map<String, String> labels = new HashMap<>();
			labels.put("age", age+"");
			labels.put(EndpointTraffic.NAME, metrics.getName());
			labels.put(EndpointTraffic.SERVICE_ID, metrics.getServiceId());
			
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
	public EndpointTraffic prometheusToSkywalking(Model model, List<Counter> metricList) {
		try {
			Counter metric = metricList.get(0);
			EndpointTraffic metrics = (EndpointTraffic) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
			metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			metrics.setServiceId(metric.getLabels().get(EndpointTraffic.SERVICE_ID));
			metrics.setName(metric.getLabels().get(EndpointTraffic.NAME));
			return metrics;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}
}
