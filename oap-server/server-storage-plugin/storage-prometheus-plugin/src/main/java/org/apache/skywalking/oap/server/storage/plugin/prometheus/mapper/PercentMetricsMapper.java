package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
			Map<String, String> labels = PrometheusMeterMapper.extractSourceColumnProperties(model, metrics);
			
			List<Sample> samples = new ArrayList<>();
			samples.add(
				new Sample(
					model.getName(), 
					new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
					metrics.getValue(), 
					TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling())
				)
			);
			
			//total
			labels.put("annotation", "total");
			samples.add(
				new Sample(
					model.getName(), 
					new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
					metrics.getTotal(), 
					TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling())
				)
			);
			
			//match
			labels.put("annotation", "match");
			samples.add(
				new Sample(
					model.getName(), 
					new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
					metrics.getMatch(), 
					TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling())
				)
			);
			
			return new MetricFamilySamples(model.getName(), Type.GAUGE, "", samples);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	@Override
	public PercentMetrics prometheusToSkywalking(Model model, List<Gauge> metricList) {
		try {
			PercentMetrics metrics = (PercentMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
			PrometheusMeterMapper.setSourceColumnsProperties(model, metrics, metricList.get(0).getLabels());
			
			try {
				setPersistenceColumns(model, metricList, metrics);
			} catch (Exception e) {
				throw new PersistenceColumnsException(e.getMessage(), e);
			}
			
			return metrics;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}

	public void setPersistenceColumns(Model model, List<Gauge> metricList, PercentMetrics metrics) {
		if(metricList.size() != 3) {
			throw new IllegalArgumentException("expect 3 metrics but found " + metricList.size());
		}
		for(Gauge metric : metricList) {
			if(!metric.getLabels().containsKey("annotation")) {
				metrics.setPercentage(new BigDecimal(metric.getValue()).intValue());
				metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			}else {
				String annotation = metric.getLabels().get("annotation");
				if(annotation.equals("total")) {
					metrics.setTotal(new BigDecimal(metric.getValue()).longValue());
				} else if(annotation.equals("match")) {
					metrics.setMatch(new BigDecimal(metric.getValue()).longValue());
				}
			}
		}
	}

}
