package org.apache.skywalking.oap.server.storage.plugin.prometheus.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.storage.IMetricsDAO;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.library.client.request.UpdateRequest;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Metric;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.JSONParser;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.MetricFamily;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi;

import com.google.gson.Gson;

import io.prometheus.client.Collector.MetricFamilySamples;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class MetricsPrometheusDAO implements IMetricsDAO {
	
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
		
		Map<String, List<Metric>> idGroup = metricFamily.getMetrics().stream().collect(Collectors.groupingBy(metrics->{
			String id = metrics.getLabels().get("id");//FIXME 会不会有漏洞
			return id;
		}));
		
		for(Entry<String, List<Metric>> entry : idGroup.entrySet()) {
			try {
				Metrics metrics = mapper.prometheusToSkywalking(model, entry.getValue());
				result.add(metrics);
			} catch (Exception e) {
				log.error("model_name:" + model.getName() + " model_class:"+model.getStorageModelClazz().getName() 
						+ " id:" + StringUtils.join(ids, ",") + e.getMessage(), e);
			}
		}
		return result;
	}
	
	@Override
	public InsertRequest prepareBatchInsert(Model model, Metrics metrics) throws IOException {
		try {
			MetricFamilySamples metricFamily = mapper.skywalkingToPrometheus(model, metrics);
			//TODO
			log.info("sw: {}, prometheus :{}", new Gson().toJson(metrics), new Gson().toJson(metricFamily));
			return new PrometheusInsertRequest(metricFamily);
		} catch (Exception e) {
			log.error("model_name:" + model.getName() + " model_class:"+model.getStorageModelClazz().getName() 
					+ e.getMessage(), e);
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	public UpdateRequest prepareBatchUpdate(Model model, Metrics metrics) throws IOException {
		return (UpdateRequest) this.prepareBatchInsert(model, metrics);
	}

}
