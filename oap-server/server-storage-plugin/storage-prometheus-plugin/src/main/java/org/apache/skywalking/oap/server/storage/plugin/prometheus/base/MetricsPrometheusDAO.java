package org.apache.skywalking.oap.server.storage.plugin.prometheus.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.storage.IMetricsDAO;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.library.client.request.UpdateRequest;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Metric;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.JSONParser;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.MetricFamily;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi;

import io.prometheus.client.Collector.MetricFamilySamples;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 每种metrics数据都会实例化一个IMetricsDAO, 需要注意的是不同的downsampling共用同一个IMetricsDAO
 * prometheus不允许更新 只能通过数据的到期时间（默认1分钟）太吃内存 FIXME
 * @author Administrator
 */
@RequiredArgsConstructor
@Slf4j
public class MetricsPrometheusDAO implements IMetricsDAO {
	
	protected final PrometheusHttpApi prometheusHttpApi;
	
	protected final PrometheusMeterMapper<Metrics, Metric> mapper;
	
//	private Map<String/*原始格式id，带时间信息*/, MetricsWithCachedtime> _quick_cache = new ConcurrentHashMap<>();
//	
//	{
//		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(
//	        new RunnableWithExceptionProtection(() -> overtimeJob(), t -> log
//	            .error("到期指标数据处理失败.", t)), 1, 1,
//	        TimeUnit.MINUTES
//	    );
//	}
//	
//	
//	
//	@RequiredArgsConstructor
//	private class MetricsWithCachedtime {
//		private final Metrics metrics;
//		private final Model model;
//		private final long cachedtime;
//	}
//	
//	private boolean isOvertime(MetricsWithCachedtime cacheItem) {
//		Model model = cacheItem.model;
//		long cachedtime = cacheItem.cachedtime;
//		long now = System.currentTimeMillis();
//		if(model.getDownsampling() == DownSampling.Minute) {
//			if(now - cachedtime >= 60000 * 1.1) {//容忍一定延迟
//				return true;
//			}
//		}else if(model.getDownsampling() == DownSampling.Hour) {
//			if(now - cachedtime >= 60 * 60000 * 1.1) {//容忍一定延迟
//				return true;
//			}
//		}else if(model.getDownsampling() == DownSampling.Day) {
//			if(now - cachedtime >= 24 * 60* 60000 * 1.1) {//容忍一定延迟
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	/**
//	 * 遍历效率太低 FIXME
//	 */
//	private void overtimeJob() {
//		List<PrometheusInsertRequest> request = new ArrayList<PrometheusInsertRequest>();
//		Set<String> idsNeedRemove = new HashSet<String>();
//		for(Entry<String, MetricsWithCachedtime> entry : _quick_cache.entrySet()) {
//			Metrics metrics = entry.getValue().metrics;
//			String id = metrics.id();
//			Model model = entry.getValue().model;
//			if(isOvertime(entry.getValue())) {
//				idsNeedRemove.add(id);
//				MetricFamilySamples metricFamily = mapper.skywalkingToPrometheus(model, metrics);
//				request.add(new PrometheusInsertRequest(metricFamily));
//			}
//		}
//		_quick_cache.entrySet().removeIf(e->idsNeedRemove.contains(e.getKey()));
//		StorageModulePrometheusProvider.cacheMonitor.dec(idsNeedRemove.size());
//		if(request.size() > 0) {
//			new Collector() {
//				@Override
//				public List<MetricFamilySamples> collect() {
//					return request.stream().filter(prepareRequst->prepareRequst != null).map(prepareRequst->((PrometheusInsertRequest) prepareRequst).getMetricFamily()).collect(Collectors.toList());
//				}
//			}.register(CustomCollectorRegistry.defaultRegistry);
//		}
//	}
//	
//	@Override
//	public List<Metrics> multiGet(Model model, List<String> ids) throws IOException {
//		List<Metrics> result = new ArrayList<Metrics>();
//		ids.stream().forEach(id->{
//			if(_quick_cache.containsKey(id)) {
//				result.add(_quick_cache.get(id).metrics);
//			}
//		});
//		return result;
//	}
//	
//	@Override
//	public InsertRequest prepareBatchInsert(Model model, Metrics metrics) throws IOException {
//		try {
//			if(_quick_cache.containsKey(metrics.id())) {
//				MetricsWithCachedtime old = _quick_cache.get(metrics.id());
//				if(isOvertime(old)) {
//					_quick_cache.remove(metrics.id());
//					StorageModulePrometheusProvider.cacheMonitor.dec();
//					MetricFamilySamples metricFamily = mapper.skywalkingToPrometheus(model, metrics);
//					new Collector() {
//						@Override
//						public List<MetricFamilySamples> collect() {
//							return Collections.singletonList(metricFamily);
//						}
//					}.register(CustomCollectorRegistry.defaultRegistry);
//				}else {
//					_quick_cache.put(metrics.id(), new MetricsWithCachedtime(metrics, model, old.cachedtime));//延用最开始的缓存时间
//				}
//			}else {
//				_quick_cache.put(metrics.id(), new MetricsWithCachedtime(metrics, model, System.currentTimeMillis()));//设置当前系统时间为缓存时间
//				StorageModulePrometheusProvider.cacheMonitor.inc();
//			}
//			return null;
//		} catch (Exception e) {
//			log.error("model_name:" + model.getName() + " model_class:"+model.getStorageModelClazz().getName() 
//					+ e.getMessage(), e);
//			throw new IOException(e.getMessage(), e);
//		}
//	}

	@Override
	public UpdateRequest prepareBatchUpdate(Model model, Metrics metrics) throws IOException {
		return (UpdateRequest) this.prepareBatchInsert(model, metrics);
	}
	
	@Override
	public List<Metrics> multiGet(Model model, List<String> ids) throws IOException {
		JSONParser parser = new JSONParser(prometheusHttpApi.query(model, ids));
		MetricFamily metricFamily = parser.parse();
		if(metricFamily == null) {
			return Collections.emptyList();
		}
		
		List<Metrics> result = new ArrayList<Metrics>();
		
		Map<String, List<Metric>> idGroup = metricFamily.getMetrics().stream().collect(Collectors.groupingBy(metrics->{
			String id = metrics.getLabels().get("id");//FIXME 会不会有漏洞
			return id;
		}));
		
		for(Entry<String, List<Metric>> entry : idGroup.entrySet()) {
			try {
				Metrics metrics = mapper.prometheusToSkywalking(model, entry.getValue());
				result.add(metrics);
			} catch (Exception e) {
				log.error("model_name:" + model.getName() + " model_class:"+model.getStorageModelClazz().getName() 
						+ " id:" + StringUtils.join(ids, ",") + e.getMessage(), e);
			}
		}
		return result;
	}
	
	@Override
	public InsertRequest prepareBatchInsert(Model model, Metrics metrics) throws IOException {
		try {
			String meanningFulId = StringUtils.substringAfter(metrics.id(), org.apache.skywalking.oap.server.core.Const.ID_CONNECTOR);
			String timebucketStr = StringUtils.substringBefore(metrics.id(), org.apache.skywalking.oap.server.core.Const.ID_CONNECTOR);
			long timestamp  = TimeBucket.getTimestamp(Long.parseLong(timebucketStr), model.getDownsampling());
			
			Map<String,String> labels = new HashMap<String, String>();
			labels.put("id", meanningFulId);
			int age = prometheusHttpApi.queryAge(model.getName(), labels, timestamp);
			if(age > 0) {
				labels.put("age", age+"");
				prometheusHttpApi.delete(model, labels, timestamp, timestamp);
			}
			MetricFamilySamples metricFamily = mapper.skywalkingToPrometheus(model, metrics, age+1);
			return new PrometheusInsertRequest(metricFamily);
		} catch (Exception e) {
			log.error("model_name:" + model.getName() + " model_class:"+model.getStorageModelClazz().getName() 
					+ e.getMessage(), e);
			throw new IOException(e.getMessage(), e);
		}
	}

}
