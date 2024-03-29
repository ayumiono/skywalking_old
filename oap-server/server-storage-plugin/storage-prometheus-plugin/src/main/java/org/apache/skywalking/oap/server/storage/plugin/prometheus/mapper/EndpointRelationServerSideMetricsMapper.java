package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.relation.endpoint.EndpointRelationServerSideMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PromeGauge;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(EndpointRelationServerSideMetrics.class)
public class EndpointRelationServerSideMetricsMapper extends PrometheusMeterMapper<EndpointRelationServerSideMetrics, PromeGauge> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, EndpointRelationServerSideMetrics metrics, int age) {
		try {
			Map<String, String> labels = new HashMap<>();
			labels.put(EndpointRelationServerSideMetrics.SOURCE_ENDPOINT, metrics.getSourceEndpoint()+"");
			labels.put(EndpointRelationServerSideMetrics.DEST_ENDPOINT, metrics.getDestEndpoint()+"");
			labels.put(EndpointRelationServerSideMetrics.COMPONENT_ID, metrics.getComponentId()+"");
			labels.put(EndpointRelationServerSideMetrics.ENTITY_ID, metrics.getEntityId()+"");
			labels.put("age", age+"");
			labels.put("id", StringUtils.substringAfter(metrics.id(), org.apache.skywalking.oap.server.core.Const.ID_CONNECTOR)); //去掉timebucket信息
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
	public EndpointRelationServerSideMetrics prometheusToSkywalking(Model model, List<PromeGauge> metricList) {
		try {
			PromeGauge metric = metricList.get(0);
			EndpointRelationServerSideMetrics metrics = (EndpointRelationServerSideMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
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
