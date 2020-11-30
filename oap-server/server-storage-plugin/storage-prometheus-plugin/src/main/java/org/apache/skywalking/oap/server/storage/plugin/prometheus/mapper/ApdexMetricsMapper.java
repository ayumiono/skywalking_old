package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.util.ArrayList;
import java.util.Collections;
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
	public MetricFamilySamples skywalkingToPrometheus(Model model, ApdexMetrics metrics) {
		Map<String, String> labels = PrometheusMeterMapper.extractMetricsColumnValues(model, metrics);
		
		labels.put("totalNum", metrics.getTotalNum()+"");
		labels.put("sNum", metrics.getSNum()+"");
		labels.put("tNum", metrics.getTNum()+"");
		
		return new MetricFamilySamples(model.getName(), Type.GAUGE, "", 
				Collections.singletonList(
						new Sample(
								model.getName(), 
								new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
								metrics.getValue(), 
								TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling())
								)));
	}

	@Override
	public ApdexMetrics prometheusToSkywalking(Model model, Gauge metric) {
		try {
			ApdexMetrics metrics = (ApdexMetrics) model.getStorageModelClazz().getDeclaredConstructor().newInstance();
			metrics.setValue(Integer.parseInt(metric.getValue()+""));
			metrics.setTimeBucket(TimeBucket.getTimeBucket(metric.getTimestamp(), model.getDownsampling()));
			
			long totalNum = Long.parseLong(metric.getLabels().get("totalNum"));
			metrics.setTotalNum(totalNum);
			long sNum = Long.parseLong(metric.getLabels().get("sNum"));
			metrics.setSNum(sNum);
			long tNum = Long.parseLong(metric.getLabels().get("tNum"));
			metrics.setTNum(tNum);
			return metrics;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return null;
		}
	}

}
