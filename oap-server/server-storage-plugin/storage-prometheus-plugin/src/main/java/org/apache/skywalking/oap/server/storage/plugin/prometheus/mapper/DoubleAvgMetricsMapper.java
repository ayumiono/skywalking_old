package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.metrics.DoubleAvgMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Gauge;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(DoubleAvgMetrics.class)
public class DoubleAvgMetricsMapper extends PrometheusMeterMapper<DoubleAvgMetrics, Gauge> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, DoubleAvgMetrics metrics) {
		
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
			
			//summation
			labels.put("annotation", "summation");
			samples.add(
				new Sample(
					model.getName(), 
					new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
					metrics.getSummation(), 
					TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling())
				)
			);
			
			//count
			labels.put("annotation", "count");
			samples.add(
				new Sample(
					model.getName(), 
					new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
					metrics.getCount(), 
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
	public DoubleAvgMetrics prometheusToSkywalking(Model model, List<Gauge> metricList) {
		try {
			DoubleAvgMetrics metrics = (DoubleAvgMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
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

	public void setPersistenceColumns(Model model, List<Gauge> metricList, DoubleAvgMetrics metrics) {
		if(metricList.size() != 3) {
			throw new IllegalArgumentException("expect 3 metrics but found " + metricList.size());
		}
		for(Gauge metric : metricList) {
			if(!metric.getLabels().containsKey("annotation")) {
				metrics.setValue(metric.getValue());
				metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			}else {
				String annotation = metric.getLabels().get("annotation");
				if(annotation.equals("summation")) {
					metrics.setSummation(new BigDecimal(metric.getValue()).longValue());
				}else if(annotation.equals("count")) {
					metrics.setCount(new BigDecimal(metric.getValue()).longValue());
				}
			}
		}
	}

}
