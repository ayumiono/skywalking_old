package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.skywalking.oap.server.core.storage.IBatchDAO;
import org.apache.skywalking.oap.server.library.client.elasticsearch.ElasticSearchInsertRequest;
import org.apache.skywalking.oap.server.library.client.elasticsearch.ElasticSearchUpdateRequest;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.library.client.request.PrepareRequest;
import org.apache.skywalking.oap.server.storage.plugin.influxdb.base.InfluxInsertRequest;
import org.apache.skywalking.oap.server.storage.plugin.jdbc.SQLExecutor;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.base.PrometheusInsertRequest;

import lombok.RequiredArgsConstructor;

/**
 * 
 * 需要按sotrageDao类型来确定用哪种存储介质 
 * @author Administrator
 *
 */
@RequiredArgsConstructor
public class IBatchDAOMixedImpl implements IBatchDAO {
	
	private final Map<String, IBatchDAO> candidates;

	@Override
	public void asynchronous(InsertRequest insertRequest) {
		if(insertRequest == null) return;
		Class<?> clazz = insertRequest.getClass();
		if(clazz == PrometheusInsertRequest.class) {
			candidates.get("prometheus").asynchronous(insertRequest);
		}else if(clazz == ElasticSearchInsertRequest.class || clazz == ElasticSearchUpdateRequest.class) {
			candidates.get("elasticsearch").asynchronous(insertRequest);
		}else if(clazz == InfluxInsertRequest.class) {
			candidates.get("influxdb").asynchronous(insertRequest);
		}else if(clazz == SQLExecutor.class) {
			candidates.get("mysql").asynchronous(insertRequest);
		}else {
			candidates.get("elasticsearch").asynchronous(insertRequest);
		}
	}

	@Override
	public void synchronous(List<PrepareRequest> prepareRequests) {
		//按类型分组
		Map<String,List<PrepareRequest>> group = prepareRequests.stream().filter(r->r!=null).collect(Collectors.groupingBy((r)->{
			Class<?> clazz = r.getClass();
			if(clazz == PrometheusInsertRequest.class) {
				return "prometheus";
			}else if(clazz == ElasticSearchInsertRequest.class || clazz == ElasticSearchUpdateRequest.class) {
				return "elasticsearch";
			}else if(clazz == InfluxInsertRequest.class) {
				return "influxdb";
			}else if(clazz == SQLExecutor.class) {
				return "mysql";
			}
			return "elasticsearch";
		}));
		for(Entry<String, List<PrepareRequest>> g : group.entrySet()) {
			candidates.get(g.getKey()).synchronous(g.getValue());
		}
	}

}
