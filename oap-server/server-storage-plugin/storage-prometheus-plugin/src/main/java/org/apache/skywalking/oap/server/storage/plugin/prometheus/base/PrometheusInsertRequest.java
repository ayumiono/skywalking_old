package org.apache.skywalking.oap.server.storage.plugin.prometheus.base;

import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.library.client.request.UpdateRequest;

import io.prometheus.client.Collector.MetricFamilySamples;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PrometheusInsertRequest implements InsertRequest, UpdateRequest {

	@Getter
	private final MetricFamilySamples metricFamily;
	
}
