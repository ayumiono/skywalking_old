package org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.source.DefaultScopeDefine;
import org.apache.skywalking.oap.server.core.source.ScopeDefaultColumn;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector.MetricFamilySamples;
import lombok.extern.slf4j.Slf4j;

/**
 * skywalking提供StorageBuilder机制，但prometheus数据结构比map更复杂，所以这里使用mapper
 * @author Administrator
 *
 * @param <SWModel>
 * @param <PromeModel>
 */
//@RequiredArgsConstructor
@Slf4j
public abstract class PrometheusMeterMapper<SWModel extends Metrics, PromeModel extends Metric> {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	final static DecimalFormat df = new DecimalFormat("#.000");
	
//	protected final StorageBuilder<Metrics> storageBuilder;

	public abstract MetricFamilySamples skywalkingToPrometheus(Model model, SWModel metrics);
	
	public abstract SWModel prometheusToSkywalking(Model model, PromeModel metric);
	
	public static void copySourceColumnsProperties(Model model, Metrics metrics, Map<String, String> labels) {
		List<ScopeDefaultColumn> columns = DefaultScopeDefine.getDefaultColumns(DefaultScopeDefine.nameOf(model.getScopeId()));
		columns.stream().forEach((sourceColumn)->{
			try {
				String typeName = sourceColumn.getType().getName();
				String columnName = sourceColumn.getColumnName();
				String fieldName = sourceColumn.getFieldName();
				Field field = metrics.getClass().getDeclaredField(fieldName);
				field.setAccessible(true);
				String columnValue = labels.get(columnName);
				switch (typeName) {
				case "long":
					field.set(metrics, Long.parseLong(columnValue));
					break;
				case "double":
					field.set(metrics, Double.parseDouble(columnValue));
					break;
				case "int":
					field.set(metrics, Integer.parseInt(columnValue));
					break;
				case "float":
					field.set(metrics, Float.parseFloat(columnValue));
					break;
				case "java.lang.String":
					field.set(metrics, columnValue);
					break;
				default:
					log.error(model.getName() + "@" + metrics.getClass().getName() + "不支持的类型字段{}", typeName);
					break;
				}
			} catch (Exception e) {
				log.error(model.getName() + "@" + metrics.getClass().getName() + e.getMessage(),e);
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}
	
	public static Map<String, String> extractMetricsColumnValues(Model model, Metrics metrics) {
		
		Map<String, String> labels = new HashMap<>();
//		Map<String, Object> objectMap = storageBuilder.data2Map(metrics);
//		for (String key : objectMap.keySet()) {
//            Object value = objectMap.get(key);
//            if (value instanceof StorageDataComplexObject) {
//            	labels.put(key, ((StorageDataComplexObject) value).toStorageData());
//            } else {
//            	labels.put(key, value.toString());
//            }
//        }
		
		//这里观察了一下,Source子类里基本上都是些基础类型的字段
		List<ScopeDefaultColumn> columns = DefaultScopeDefine.getDefaultColumns(DefaultScopeDefine.nameOf(model.getScopeId()));
		
//		model.getColumns().stream().forEach(column->{ 不能使用Model中的column，因为其中包括Metrics子类的字段，extractMetricsColumnValues方法只需要抽出Source中的字段做标签
//			try {
//				ColumnName columnName = column.getColumnName();
//				Field field = metrics.getClass().getDeclaredField(columnName.getName());
//				field.setAccessible(true);
//				Object value = field.get(metrics);
//				labels.put(columnName.getName(), value.toString());
//			} catch (Exception e) {
//				throw new RuntimeException(e.getMessage(), e);
//			}
//		});
		
		columns.stream().forEach((sourceColumn)->{
			try {
				Field field = metrics.getClass().getDeclaredField(sourceColumn.getFieldName());
				field.setAccessible(true);
				Object value = field.get(metrics);
				labels.put(sourceColumn.getColumnName(), value.toString());
			} catch (Exception e) {
				log.error(model.getName() + "@" + metrics.getClass().getName() + e.getMessage(),e);
				throw new RuntimeException(e.getMessage(), e);
			}
		});
		
		labels.put("id", StringUtils.substringAfter(metrics.id(), org.apache.skywalking.oap.server.core.Const.ID_CONNECTOR)); //去掉timebucket信息
		return labels;
	}
	
	/**
	 * 有些Metrics的id格式不太一样，比如EndpointTraffic
	 * @param model
	 * @param ids
	 * @return
	 */
	public static Map<String/*timestamp形如1606811026.446*/, Set<String>> idAndTimestampTuple2(Model model, List<String> ids) {
		Map<String, Set<String>> rs = new HashMap<>();
		try {
			for(String id : ids) {
				String meanningFulId = StringUtils.substringAfter(id, org.apache.skywalking.oap.server.core.Const.ID_CONNECTOR);
				String timebucketStr = StringUtils.substringBefore(id, org.apache.skywalking.oap.server.core.Const.ID_CONNECTOR);
				long timestamp  = TimeBucket.getTimestamp(Long.parseLong(timebucketStr), model.getDownsampling());
				String ts = df.format(new BigDecimal(timestamp+"").divide(new BigDecimal("1000")).doubleValue());
				
				if(!rs.containsKey(ts)) {
					rs.put(ts, new HashSet<>());
				}
				rs.get(ts).add(meanningFulId);
			}
		} catch (Exception e) {
			log.error("idAndTimestampTuple2 failed {}", model.getName());
		}
		return rs;
	}
}
