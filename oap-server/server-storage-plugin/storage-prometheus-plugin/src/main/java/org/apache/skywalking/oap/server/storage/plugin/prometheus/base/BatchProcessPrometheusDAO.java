package org.apache.skywalking.oap.server.storage.plugin.prometheus.base;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.skywalking.oap.server.core.storage.IBatchDAO;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.library.client.request.PrepareRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.PushGateway;

/**
 * 
 * PushGateway.pushAdd(Collector collector, String job)
 * TODO 需要将Collector包装成skywalking中的InsertRequest, PrepareRequest
 * @author Administrator
 *
 */
public class BatchProcessPrometheusDAO implements IBatchDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessPrometheusDAO.class);
	
	private final PushGateway pushGateway;
	
	public BatchProcessPrometheusDAO(PushGateway pushGateway) {
		this.pushGateway = pushGateway;
	}

	@Override
	public void asynchronous(InsertRequest insertRequest) {
		// TODO Auto-generated method stub
	}

	@Override
	public void synchronous(List<PrepareRequest> prepareRequests) {
		try {
			new Collector() {//Custom Collectors
				@Override
				public List<MetricFamilySamples> collect() {
					return prepareRequests.stream().map(prepareRequst->(MetricFamilySamples)prepareRequests).collect(Collectors.toList());
				}
			}.register();
			pushGateway.push(CollectorRegistry.defaultRegistry, "");
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
		}
	}

}
