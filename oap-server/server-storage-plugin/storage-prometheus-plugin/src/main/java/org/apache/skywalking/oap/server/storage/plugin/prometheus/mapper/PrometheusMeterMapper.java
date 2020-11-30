package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.storage.model.ColumnName;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector.MetricFamilySamples;

/**
 * skywalking提供StorageBuilder机制，但prometheus数据结构比map更复杂，所以这里使用mapper
 * @author Administrator
 *
 * @param <SWModel>
 * @param <PromeModel>
 */
public abstract class PrometheusMeterMapper<SWModel extends Metrics, PromeModel extends Metric> {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public abstract MetricFamilySamples skywalkingToPrometheus(Model model, SWModel metrics);
	
	public abstract SWModel prometheusToSkywalking(Model model, PromeModel metric);
	
	public static Map<String, String> extractMetricsColumnValues(Model model, Metrics metrics) {
		Map<String, String> labels = new HashMap<>();
		model.getColumns().stream().forEach(column->{
			try {
				ColumnName columnName = column.getColumnName();
				Field field = metrics.getClass().getDeclaredField(columnName.getName());
				field.setAccessible(true);
				Object value = field.get(metrics);
				labels.put(columnName.getName(), value.toString());
			} catch (Exception e) {
				throw new RuntimeException("");
			}
		});
		labels.put("id", metrics.id());
		return labels;
	}
}
