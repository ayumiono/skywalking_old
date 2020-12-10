package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.apm.util.StringUtil;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.endpoint.EndpointTraffic;
import org.apache.skywalking.oap.server.core.analysis.manual.instance.InstanceTraffic;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Gauge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(InstanceTraffic.class)
public class InstanceTrafficMapper extends PrometheusMeterMapper<InstanceTraffic, Gauge> {
	
	private static final Gson GSON = new Gson();

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, InstanceTraffic metrics, int age) {
		try {
			Map<String, String> labels = new HashMap<>();
			labels.put(InstanceTraffic.NAME, metrics.getName());
			labels.put(InstanceTraffic.SERVICE_ID, metrics.getServiceId());
			labels.put("age", age+"");
			if (metrics.getProperties() != null) {
				labels.put(InstanceTraffic.PROPERTIES, GSON.toJson(metrics.getProperties()));
            } else {
            	labels.put(InstanceTraffic.PROPERTIES, Const.EMPTY_STRING);
            }
			labels.put(InstanceTraffic.LAST_PING_TIME_BUCKET, metrics.getLastPingTimestamp()+"");
			
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
	public InstanceTraffic prometheusToSkywalking(Model model, List<Gauge> metricList) {
		try {
			Gauge metric = metricList.get(0);
			InstanceTraffic metrics = (InstanceTraffic) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
			metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			metrics.setServiceId(metric.getLabels().get(EndpointTraffic.SERVICE_ID));
			metrics.setName(metric.getLabels().get(EndpointTraffic.NAME));
			final String propString = metric.getLabels().get(InstanceTraffic.PROPERTIES);
	        if (StringUtil.isNotEmpty(propString)) {
	        	metrics.setProperties(GSON.fromJson(propString, JsonObject.class));
	        }
	        metrics.setLastPingTimestamp(Long.parseLong(metric.getLabels().get(InstanceTraffic.LAST_PING_TIME_BUCKET)));
			return metrics;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}
}
