package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.metrics.ApdexMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Gauge;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(ApdexMetrics.class)
public class ApdexMetricsMapper extends PrometheusMeterMapper<ApdexMetrics, Gauge> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, ApdexMetrics metrics, int age) {
		Map<String, String> labels = PrometheusMeterMapper.extractSourceColumnProperties(model, metrics);
		labels.put("age", age+"");
		
		List<Sample> samples = new ArrayList<>();
		samples.add(
			new Sample(
				model.getName(), 
				new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
				metrics.getValue(), 
				TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling())
			)
		);
		//totalNum
		labels.put("annotation", "totalNum");
		samples.add(
			new Sample(
				model.getName(), 
				new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
				metrics.getTotalNum(), 
				TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling())
			)
		);
		//sNum
		labels.put("annotation", "sNum");
		samples.add(
			new Sample(
				model.getName(), 
				new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
				metrics.getSNum(), 
				TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling())
			)
		);
		//tNum
		labels.put("annotation", "tNum");
		samples.add(
			new Sample(
				model.getName(), 
				new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
				metrics.getTNum(), 
				TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling())
			)
		);
		
		return new MetricFamilySamples(model.getName(), Type.GAUGE, "", samples);
	}
	
	@Override
	public ApdexMetrics prometheusToSkywalking(Model model, List<Gauge> metricList) {
		try {
			ApdexMetrics metrics = (ApdexMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
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

	public void setPersistenceColumns(Model model, List<Gauge> metricList, ApdexMetrics metrics) {
		if(metricList.size() != 4) {
			throw new IllegalArgumentException("expect 4 metrics but found " + metricList.size());
		}
		for(Gauge metric : metricList) {
			if(!metric.getLabels().containsKey("annotation")) {
				metrics.setValue(new BigDecimal(metric.getValue()).intValue());
				metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			}else {
				String annotation = metric.getLabels().get("annotation");
				if(annotation.equals("totalNum")) {
					metrics.setTotalNum(new BigDecimal(metric.getValue()).longValue());
				} else if(annotation.equals("sNum")) {
					metrics.setSNum(new BigDecimal(metric.getValue()).longValue());
				} else if(annotation.equals("tNum")) {
					metrics.setTNum(new BigDecimal(metric.getValue()).longValue());
				}
			}
		}
	}

}
