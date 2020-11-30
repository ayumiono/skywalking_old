package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import org.apache.skywalking.oap.server.core.analysis.manual.networkalias.NetworkAddressAlias;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Counter;

import io.prometheus.client.Collector.MetricFamilySamples;

@PrometheusMetricsMapper(NetworkAddressAlias.class)
public class NetworkAddressAliasMapper extends PrometheusMeterMapper<NetworkAddressAlias, Counter> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, NetworkAddressAlias metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetworkAddressAlias prometheusToSkywalking(Model model, Counter metric) {
		// TODO Auto-generated method stub
		return null;
	}

}
