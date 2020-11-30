package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.metrics.PercentMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Gauge;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(PercentMetrics.class)
public class PercentMetricsMapper extends PrometheusMeterMapper<PercentMetrics, Gauge> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, PercentMetrics metrics) {
		try {
			Map<String, String> labels = PrometheusMeterMapper.extractMetricsColumnValues(model, metrics);
			
			labels.put("total", metrics.getTotal()+"");
			labels.put("match", metrics.getMatch()+"");
			
			return new MetricFamilySamples(model.getName(), Type.GAUGE, "", 
					Collections.singletonList(
							new Sample(
									model.getName(), 
									new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
									metrics.getValue(), 
									TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling())
									)));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public PercentMetrics prometheusToSkywalking(Model model, Gauge metric) {
		try {
			PercentMetrics metrics = (PercentMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
			metrics.setPercentage(Integer.parseInt(metric.getValue()+""));
			metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			
			long total = Long.parseLong(metric.getLabels().get("total"));
			metrics.setTotal(total);
			long match = Long.parseLong(metric.getLabels().get("match"));
			metrics.setMatch(match);
			return metrics;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}

}
