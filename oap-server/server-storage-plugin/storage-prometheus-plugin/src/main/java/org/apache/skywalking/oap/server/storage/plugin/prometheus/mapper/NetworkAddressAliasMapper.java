package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.networkalias.NetworkAddressAlias;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PromeGauge;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(NetworkAddressAlias.class)
public class NetworkAddressAliasMapper extends PrometheusMeterMapper<NetworkAddressAlias, PromeGauge> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, NetworkAddressAlias metrics, int age) {
		try {
			Map<String, String> labels = new HashMap<>();
			labels.put("age", age+"");
            labels.put("address", metrics.getAddress());
            labels.put("represent_service_id", metrics.getRepresentServiceId());
            labels.put("represent_service_instance_id", metrics.getRepresentServiceInstanceId());
			return new MetricFamilySamples(model.getName(), Type.COUNTER, "", 
					Collections.singletonList(
							new Sample(
									model.getName(), 
									new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
									metrics.getLastUpdateTimeBucket(), 
									TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling())
									)));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public NetworkAddressAlias prometheusToSkywalking(Model model, List<PromeGauge> metricList) {
		try {
			PromeGauge metric = metricList.get(0);
			NetworkAddressAlias metrics = (NetworkAddressAlias) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
			Map<String, String> labels = metric.getLabels();
			metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			metrics.setAddress(labels.get("address"));
			metrics.setRepresentServiceId(labels.get("represent_service_id"));
			metrics.setRepresentServiceInstanceId(labels.get("represent_service_instance_id"));
			metrics.setLastUpdateTimeBucket(Long.parseLong(metric.getValue()+""));
			return metrics;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}
}
