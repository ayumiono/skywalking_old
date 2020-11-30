package org.apache.skywalking.oap.server.storage.plugin.prometheus.base;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.storage.IMetricsDAO;
import org.apache.skywalking.oap.server.core.storage.StorageBuilder;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.library.client.request.UpdateRequest;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Metric;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.MetricFamily;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.JSONParser;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector.MetricFamilySamples;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MetricsPrometheusDAO implements IMetricsDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MetricsPrometheusDAO.class);
	
	protected final StorageBuilder<Metrics> storageBuilder;
	
	protected final PrometheusHttpApi prometheusHttpApi;
	
	protected final PrometheusMeterMapper<Metrics, Metric> mapper;
	
	@Override
	public List<Metrics> multiGet(Model model, List<String> ids) throws IOException {
		JSONParser parser = new JSONParser(prometheusHttpApi.rangeQuery(model, ids, 0l,0l));
		MetricFamily metricFamily = parser.parse();
		
		return metricFamily.getMetrics().stream().map(promeMetric->{
			try {
				return mapper.prometheusToSkywalking(model, promeMetric);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}
			return (Metrics)null;
		}).collect(Collectors.toList());
	}
	
	@Override
	public InsertRequest prepareBatchInsert(Model model, Metrics metrics) throws IOException {
		try {
			MetricFamilySamples metricFamily = mapper.skywalkingToPrometheus(model, metrics);
			return new PrometheusInsertRequest(metricFamily);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		return null;
	}

	@Override
	public UpdateRequest prepareBatchUpdate(Model model, Metrics metrics) throws IOException {
		return (UpdateRequest) this.prepareBatchInsert(model, metrics);
	}

}
