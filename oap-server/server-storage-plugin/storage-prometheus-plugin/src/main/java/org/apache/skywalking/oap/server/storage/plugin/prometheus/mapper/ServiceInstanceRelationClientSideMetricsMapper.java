package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.relation.instance.ServiceInstanceRelationClientSideMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PromeGauge;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(ServiceInstanceRelationClientSideMetrics.class)
public class ServiceInstanceRelationClientSideMetricsMapper extends PrometheusMeterMapper<ServiceInstanceRelationClientSideMetrics, PromeGauge> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, ServiceInstanceRelationClientSideMetrics metrics, int age) {
		try {
			Map<String, String> labels = new HashMap<>();
			labels.put("age", age+"");
			labels.put("id", StringUtils.substringAfter(metrics.id(), org.apache.skywalking.oap.server.core.Const.ID_CONNECTOR)); //去掉timebucket信息
            
			labels.put(ServiceInstanceRelationClientSideMetrics.ENTITY_ID, metrics.getEntityId());
            labels.put(ServiceInstanceRelationClientSideMetrics.SOURCE_SERVICE_ID, metrics.getSourceServiceId());
            labels.put(ServiceInstanceRelationClientSideMetrics.SOURCE_SERVICE_INSTANCE_ID, metrics.getSourceServiceInstanceId());
            labels.put(ServiceInstanceRelationClientSideMetrics.DEST_SERVICE_ID, metrics.getDestServiceId());
            labels.put(ServiceInstanceRelationClientSideMetrics.DEST_SERVICE_INSTANCE_ID, metrics.getDestServiceInstanceId());
            labels.put(ServiceInstanceRelationClientSideMetrics.COMPONENT_ID, metrics.getComponentId()+"");
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
	public ServiceInstanceRelationClientSideMetrics prometheusToSkywalking(Model model, List<PromeGauge> metricList) {
		try {
			PromeGauge metric = metricList.get(0);
			ServiceInstanceRelationClientSideMetrics metrics = (ServiceInstanceRelationClientSideMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
			Map<String, String> labels = metric.getLabels();
			metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			metrics.setEntityId(labels.get(ServiceInstanceRelationClientSideMetrics.ENTITY_ID));
			metrics.setSourceServiceId(labels.get(ServiceInstanceRelationClientSideMetrics.SOURCE_SERVICE_ID));
			metrics.setSourceServiceInstanceId(labels.get(ServiceInstanceRelationClientSideMetrics.SOURCE_SERVICE_INSTANCE_ID));
			metrics.setDestServiceId(labels.get(ServiceInstanceRelationClientSideMetrics.DEST_SERVICE_ID));
			metrics.setDestServiceInstanceId(labels.get(ServiceInstanceRelationClientSideMetrics.DEST_SERVICE_INSTANCE_ID));
			metrics.setComponentId(Integer.parseInt(labels.get(ServiceInstanceRelationClientSideMetrics.COMPONENT_ID)));
			return metrics;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}
}
