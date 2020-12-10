package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.NodeType;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.service.ServiceTraffic;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Counter;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(ServiceTraffic.class)
@Deprecated
public class ServiceTrafficMapper extends PrometheusMeterMapper<ServiceTraffic, Counter> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, ServiceTraffic metrics, int age) {
		try {
			Map<String, String> labels = new HashMap<>();
			labels.put("age", age+"");
            labels.put(ServiceTraffic.NAME, metrics.getName());
            labels.put(ServiceTraffic.NODE_TYPE, metrics.getNodeType().value()+"");
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
	public ServiceTraffic prometheusToSkywalking(Model model, List<Counter> metricList) {
		try {
			Counter metric = metricList.get(0);
			ServiceTraffic metrics = (ServiceTraffic) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
			Map<String, String> labels = metric.getLabels();
			metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			metrics.setName(labels.get(ServiceTraffic.NAME));
			metrics.setNodeType(NodeType.valueOf(labels.get(ServiceTraffic.NODE_TYPE)));
			return metrics;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}
}
