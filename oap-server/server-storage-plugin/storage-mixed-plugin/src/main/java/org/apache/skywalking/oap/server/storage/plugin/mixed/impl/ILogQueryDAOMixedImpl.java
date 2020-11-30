package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.skywalking.oap.server.core.query.type.LogState;
import org.apache.skywalking.oap.server.core.query.type.Logs;
import org.apache.skywalking.oap.server.core.query.type.Pagination;
import org.apache.skywalking.oap.server.core.storage.query.ILogQueryDAO;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ILogQueryDAOMixedImpl implements ILogQueryDAO {

	private final StorageModuleMixedConfig config;

	private final Map<String/* provider */, ILogQueryDAO> candidates;

	@Override
	public Logs queryLogs(String metricName, int serviceId, int serviceInstanceId, String endpointId, String traceId,
			LogState state, String stateCode, Pagination paging, int from, int limit, long startTB, long endTB)
			throws IOException {
		return candidates.get(config.getRecord()).queryLogs(metricName, serviceId, serviceInstanceId, endpointId, traceId, state, stateCode, paging, from, limit, startTB, endTB);
	}

}
