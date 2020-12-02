package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.networkalias.NetworkAddressAlias;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Counter;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.Type;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;

@PrometheusMetricsMapper(NetworkAddressAlias.class)
public class NetworkAddressAliasMapper extends PrometheusMeterMapper<NetworkAddressAlias, Counter> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, NetworkAddressAlias metrics) {
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
	public NetworkAddressAlias prometheusToSkywalking(Model model, Counter metric) {
		// TODO Auto-generated method stub
		return null;
	}

}
