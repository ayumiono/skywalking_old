package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.metrics.DataTable;
import org.apache.skywalking.oap.server.core.analysis.metrics.PercentileMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PromeSummary;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(PercentileMetrics.class)
public class PercentileMetricsMapper extends PrometheusMeterMapper<PercentileMetrics, PromeSummary> {

	private static final int[] RANKS = {
	        50,
	        75,
	        90,
	        95,
	        99
	};
	
	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, PercentileMetrics metrics, int age) {
		try {
			Type type = Type.SUMMARY;
			String name = model.getName();
			Map<String, String> labels = PrometheusMeterMapper.extractSourceColumnProperties(model, metrics);
			labels.put("age", age+"");
			long timestamp = TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling());
			
			List<Sample> samples = new ArrayList<>();
			labels.put("precision", metrics.getPrecision()+"");
			
			Sample sumSample = new Sample(name+"_sum", new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
					metrics.getDataset().sumOfValues(), timestamp);
			Sample countSample = new Sample(name + "_count", new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
					metrics.getDataset().size(), timestamp);
			samples.add(sumSample);samples.add(countSample);
			metrics.getPercentileValues().keys().forEach(key->{
				long value = metrics.getPercentileValues().get(key);
				labels.put("quantile", key);
				Sample sample = new Sample(name, new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), value, timestamp);
				samples.add(sample);
			});
			labels.remove("quantile");
			metrics.getDataset().keys().forEach(key->{
				long value = metrics.getDataset().get(key);
				labels.put("le", key);
				Sample sample = new Sample(name+"_bucket", new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), value, timestamp);
				samples.add(sample);
			});
			return new MetricFamilySamples(name, type, "", samples);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}
	
	@Override
	public PercentileMetrics prometheusToSkywalking(Model model, List<PromeSummary> metricList) {
		try {
			PromeSummary metric = metricList.get(0);
			PercentileMetrics metrics = (PercentileMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
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

	public void setPersistenceColumns(Model model, PromeSummary metric, PercentileMetrics metrics) {
		metrics.setDataset(toDataTable(metric.getDataset()));
		metrics.setPercentileValues(toDataTable(metric.getQuantiles()));
		metrics.setPrecision(metric.getPrecision());
		metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
	}
	
	private DataTable toDataTable(Map<String, Long> ds) {
		DataTable dtable = new DataTable();
		ds.entrySet().stream().forEach((entry)->{
			dtable.put(entry.getKey(), entry.getValue());
		});
		return dtable;
	}

}
