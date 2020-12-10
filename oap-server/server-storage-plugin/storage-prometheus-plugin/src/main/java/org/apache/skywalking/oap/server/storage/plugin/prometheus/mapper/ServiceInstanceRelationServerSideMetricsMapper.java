package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.relation.instance.ServiceInstanceRelationServerSideMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Gauge;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(ServiceInstanceRelationServerSideMetrics.class)
public class ServiceInstanceRelationServerSideMetricsMapper extends PrometheusMeterMapper<ServiceInstanceRelationServerSideMetrics, Gauge> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, ServiceInstanceRelationServerSideMetrics metrics, int age) {
		try {
			Map<String, String> labels = new HashMap<>();
			labels.put("age", age+"");
			labels.put("id", StringUtils.substringAfter(metrics.id(), org.apache.skywalking.oap.server.core.Const.ID_CONNECTOR)); //去掉timebucket信息
            
            labels.put(ServiceInstanceRelationServerSideMetrics.ENTITY_ID, metrics.getEntityId());
            labels.put(ServiceInstanceRelationServerSideMetrics.SOURCE_SERVICE_ID, metrics.getSourceServiceId());
            labels.put(ServiceInstanceRelationServerSideMetrics.SOURCE_SERVICE_INSTANCE_ID, metrics.getSourceServiceInstanceId());
            labels.put(ServiceInstanceRelationServerSideMetrics.DEST_SERVICE_ID, metrics.getDestServiceId());
            labels.put(ServiceInstanceRelationServerSideMetrics.DEST_SERVICE_INSTANCE_ID, metrics.getDestServiceInstanceId());
            labels.put(ServiceInstanceRelationServerSideMetrics.COMPONENT_ID, metrics.getComponentId()+"");
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
	public ServiceInstanceRelationServerSideMetrics prometheusToSkywalking(Model model, List<Gauge> metricList) {
		try {
			Gauge metric = metricList.get(0);
			ServiceInstanceRelationServerSideMetrics metrics = (ServiceInstanceRelationServerSideMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
			Map<String, String> labels = metric.getLabels();
			metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			metrics.setEntityId(labels.get(ServiceInstanceRelationServerSideMetrics.ENTITY_ID));
			metrics.setSourceServiceId(labels.get(ServiceInstanceRelationServerSideMetrics.SOURCE_SERVICE_ID));
			metrics.setSourceServiceInstanceId(labels.get(ServiceInstanceRelationServerSideMetrics.SOURCE_SERVICE_INSTANCE_ID));
			metrics.setDestServiceId(labels.get(ServiceInstanceRelationServerSideMetrics.DEST_SERVICE_ID));
			metrics.setDestServiceInstanceId(labels.get(ServiceInstanceRelationServerSideMetrics.DEST_SERVICE_INSTANCE_ID));
			metrics.setComponentId(Integer.parseInt(labels.get(ServiceInstanceRelationServerSideMetrics.COMPONENT_ID)));
			return metrics;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}
}
