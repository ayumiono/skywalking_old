package org.apache.skywalking.oap.server.storage.plugin.prometheus.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

import io.prometheus.client.Collector.MetricFamilySamples;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class MetricsPrometheusDAO implements IMetricsDAO {
	
	protected final StorageBuilder<Metrics> storageBuilder;
	
	protected final PrometheusHttpApi prometheusHttpApi;
	
	protected final PrometheusMeterMapper<Metrics, Metric> mapper;
	
	@Override
	public List<Metrics> multiGet(Model model, List<String> ids) throws IOException {
		JSONParser parser = new JSONParser(prometheusHttpApi.query(model, ids));
		MetricFamily metricFamily = parser.parse();
		if(metricFamily == null) {
			return Collections.emptyList();
		}
		
		List<Metrics> result = new ArrayList<Metrics>();
		
		for(Metric promeMetric : metricFamily.getMetrics()) {
			try {
				Metrics metrics = mapper.prometheusToSkywalking(model, promeMetric);
				result.add(metrics);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return result;
	}
	
	@Override
	public InsertRequest prepareBatchInsert(Model model, Metrics metrics) throws IOException {
		try {
			MetricFamilySamples metricFamily = mapper.skywalkingToPrometheus(model, metrics);
			return new PrometheusInsertRequest(metricFamily);
		} catch (Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	public UpdateRequest prepareBatchUpdate(Model model, Metrics metrics) throws IOException {
		return (UpdateRequest) this.prepareBatchInsert(model, metrics);
	}

}
