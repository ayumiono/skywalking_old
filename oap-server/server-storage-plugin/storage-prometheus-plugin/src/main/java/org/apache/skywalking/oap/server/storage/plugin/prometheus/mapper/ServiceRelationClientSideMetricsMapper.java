package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.relation.service.ServiceRelationClientSideMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Gauge;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(ServiceRelationClientSideMetrics.class)
public class ServiceRelationClientSideMetricsMapper extends PrometheusMeterMapper<ServiceRelationClientSideMetrics, Gauge> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, ServiceRelationClientSideMetrics metrics, int age) {
		try {
			Map<String, String> labels = new HashMap<>();
			labels.put("age", age+"");
			labels.put("id", StringUtils.substringAfter(metrics.id(), org.apache.skywalking.oap.server.core.Const.ID_CONNECTOR)); //去掉timebucket信息
            
            labels.put(ServiceRelationClientSideMetrics.SOURCE_SERVICE_ID, metrics.getSourceServiceId());
            labels.put(ServiceRelationClientSideMetrics.DEST_SERVICE_ID, metrics.getDestServiceId());
            labels.put(ServiceRelationClientSideMetrics.COMPONENT_ID, metrics.getComponentId()+"");
            labels.put(ServiceRelationClientSideMetrics.ENTITY_ID, metrics.getEntityId());
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
	public ServiceRelationClientSideMetrics prometheusToSkywalking(Model model, List<Gauge> metricList) {
		try {
			Gauge metric = metricList.get(0);
			ServiceRelationClientSideMetrics metrics = (ServiceRelationClientSideMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
			Map<String, String> labels = metric.getLabels();
			metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			metrics.setComponentId(Integer.parseInt(labels.get(ServiceRelationClientSideMetrics.COMPONENT_ID)));
			metrics.setEntityId(labels.get(ServiceRelationClientSideMetrics.ENTITY_ID));
			metrics.setSourceServiceId(labels.get(ServiceRelationClientSideMetrics.SOURCE_SERVICE_ID));
			metrics.setDestServiceId(labels.get(ServiceRelationClientSideMetrics.DEST_SERVICE_ID));
			return metrics;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}
}
