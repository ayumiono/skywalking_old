package org.apache.skywalking.oap.server.storage.plugin.prometheus.base;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.skywalking.oap.server.core.storage.IBatchDAO;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.library.client.request.PrepareRequest;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.CustomCollectorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BatchProcessPrometheusDAO implements IBatchDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessPrometheusDAO.class);
	
	@Override
	public void asynchronous(InsertRequest insertRequest) {
		new Collector() {
			@Override
			public List<MetricFamilySamples> collect() {
				return Collections.singletonList((MetricFamilySamples) insertRequest);
			}
		}.register(CustomCollectorRegistry.defaultRegistry);
	}

	@Override
	public void synchronous(List<PrepareRequest> prepareRequests) {
		try {
			new Collector() {
				@Override
				public List<MetricFamilySamples> collect() {
					return prepareRequests.stream().filter(prepareRequst->prepareRequst != null).map(prepareRequst->((PrometheusInsertRequest) prepareRequst).getMetricFamily()).collect(Collectors.toList());
				}
			}.register(CustomCollectorRegistry.defaultRegistry);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

}
