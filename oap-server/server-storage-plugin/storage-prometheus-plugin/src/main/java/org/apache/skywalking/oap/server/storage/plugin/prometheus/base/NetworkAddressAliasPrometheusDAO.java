package org.apache.skywalking.oap.server.storage.plugin.prometheus.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.DownSampling;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.networkalias.NetworkAddressAlias;
import org.apache.skywalking.oap.server.core.storage.cache.INetworkAddressAliasDAO;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Metric;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.JSONParser;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PromeMetric;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PromeMetricFamily;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class NetworkAddressAliasPrometheusDAO implements INetworkAddressAliasDAO {

	private final PrometheusHttpApi api;

	@Override
	public List<NetworkAddressAlias> loadLastUpdate(long timeBucket) {
		List<NetworkAddressAlias> result = new ArrayList<NetworkAddressAlias>();
		try {
			long start = TimeBucket.getTimestamp(timeBucket, DownSampling.Minute);

			JSONParser parser = new JSONParser(api.rangeQuery(NetworkAddressAlias.INDEX_NAME, null, start));

			PromeMetricFamily mf = parser.parse();

			if (mf == null) {
				return result;
			}

			for (PromeMetric promeMetric : mf.getMetrics()) {
				NetworkAddressAlias model = new NetworkAddressAlias();
				Map<String, String> labels = promeMetric.getLabels();
				long timestamp = promeMetric.getTimestamp();
				model.setAddress(labels.get("address"));
				model.setRepresentServiceId(labels.get("represent_service_id"));
				model.setRepresentServiceInstanceId(labels.get("represent_service_instance_id"));
				model.setLastUpdateTimeBucket(TimeBucket.getMinuteTimeBucket(timestamp));
				model.setTimeBucket(TimeBucket.getMinuteTimeBucket(timestamp));
				result.add(model);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

}
