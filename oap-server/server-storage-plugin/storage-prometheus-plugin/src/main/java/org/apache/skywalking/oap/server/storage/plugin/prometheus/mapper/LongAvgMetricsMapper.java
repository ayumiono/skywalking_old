package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.metrics.LongAvgMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Gauge;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(LongAvgMetrics.class)
public class LongAvgMetricsMapper extends PrometheusMeterMapper<LongAvgMetrics, Gauge> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, LongAvgMetrics metrics) {
		
		try {
			Map<String, String> labels = PrometheusMeterMapper.extractMetricsColumnValues(model, metrics);
			
			labels.put("summation", metrics.getSummation()+"");
			labels.put("count", metrics.getCount()+"");
			
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
	public LongAvgMetrics prometheusToSkywalking(Model model, Gauge metric) {
		try {
			LongAvgMetrics metrics = (LongAvgMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
			metrics.setValue(Long.parseLong(metric.getValue()+""));
			metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			
			long summation = Long.parseLong(metric.getLabels().get("summation"));
			metrics.setSummation(summation);
			long count = Long.parseLong(metric.getLabels().get("count"));
			metrics.setCount(count);
			return metrics;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}

}
