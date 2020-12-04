package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.metrics.DataTable;
import org.apache.skywalking.oap.server.core.analysis.metrics.HistogramMetrics;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PromeHistogram;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;

@PrometheusMetricsMapper(HistogramMetrics.class)
public class HistogramMetricsMapper extends PrometheusMeterMapper<HistogramMetrics, PromeHistogram> {

	@Override
	public MetricFamilySamples skywalkingToPrometheus(Model model, HistogramMetrics metrics) {
		try {
			Type type = Type.HISTOGRAM;
			String name = model.getName();
			Map<String, String> labels = PrometheusMeterMapper.extractSourceColumnProperties(model, metrics);
			long timestamp = TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling());
			
			List<Sample> samples = new ArrayList<>();
			
			HistogramMetrics _metrics = (HistogramMetrics) metrics;
			Sample sumSample = new Sample(name+"_sum", new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), _metrics.getDataset().sumOfValues(), timestamp);
			Sample countSample = new Sample(name + "_count", new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), _metrics.getDataset().size(), timestamp);
			samples.add(sumSample);samples.add(countSample);
			_metrics.getDataset().keys().forEach(key->{
				long value = _metrics.getDataset().get(key);
				labels.put("le", key);
				Sample sample = new Sample(name+"_bucket", new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), value, timestamp);
				samples.add(sample);
			});
			
			return new MetricFamilySamples(name, type, "", samples);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public HistogramMetrics prometheusToSkywalking(Model model, List<PromeHistogram> metricList) {
		try {
			
			PromeHistogram metric = metricList.get(0);
			
			HistogramMetrics metrics = (HistogramMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
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
	
	public void setPersistenceColumns(Model model, PromeHistogram metric, HistogramMetrics metrics) {
		DataTable dt = new DataTable(metric.getBuckets().size());
			metric.getBuckets().entrySet().stream().forEach(entry->{
			dt.put(entry.getKey()+"", entry.getValue());
		});
		metrics.setDataset(dt);
		metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
	}

}
