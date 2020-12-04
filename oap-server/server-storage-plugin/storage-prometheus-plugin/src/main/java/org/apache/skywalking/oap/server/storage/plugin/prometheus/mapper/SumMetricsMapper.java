package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.metrics.SumMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Gauge;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(SumMetrics.class)
public class SumMetricsMapper extends PrometheusMeterMapper<SumMetrics, Gauge>{

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, SumMetrics metrics) {
		try {
			Map<String, String> labels = PrometheusMeterMapper.extractSourceColumnProperties(model, metrics);
			
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
	public SumMetrics prometheusToSkywalking(Model model, List<Gauge> metricList) {
		try {
			Gauge metric = metricList.get(0);
			SumMetrics metrics = (SumMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
			PrometheusMeterMapper.setSourceColumnsProperties(model, metrics, metric.getLabels());
			
			try {
				setPersistenceColumns(model, metric, metrics);
			} catch (Exception e) {
				throw new PersistenceColumnsException(e.getMessage(), e);
			}
			
			return metrics;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}

	public void setPersistenceColumns(Model model, Gauge metric, SumMetrics metrics) {
		metrics.setValue(new BigDecimal(metric.getValue()).longValue());
		metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
	}

}
