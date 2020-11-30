package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.manual.segment.SegmentRecord;
import org.apache.skywalking.oap.server.core.query.type.QueryOrder;
import org.apache.skywalking.oap.server.core.query.type.Span;
import org.apache.skywalking.oap.server.core.query.type.TraceBrief;
import org.apache.skywalking.oap.server.core.query.type.TraceState;
import org.apache.skywalking.oap.server.core.storage.query.ITraceQueryDAO;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ITraceQueryDAOMixedImpl implements ITraceQueryDAO {

	private final StorageModuleMixedConfig config;
	
	private final Map<String/*provider*/, ITraceQueryDAO> candidates;
	
	@Override
	public TraceBrief queryBasicTraces(long startSecondTB, long endSecondTB, long minDuration, long maxDuration,
			String endpointName, String serviceId, String serviceInstanceId, String endpointId, String traceId,
			int limit, int from, TraceState traceState, QueryOrder queryOrder) throws IOException {
		return candidates.get(config.getRecord()).queryBasicTraces(startSecondTB, endSecondTB, minDuration, maxDuration, endpointName, serviceId, serviceInstanceId, endpointId, traceId, limit, from, traceState, queryOrder);
	}

	@Override
	public List<SegmentRecord> queryByTraceId(String traceId) throws IOException {
		return candidates.get(config.getRecord()).queryByTraceId(traceId);
	}

	@Override
	public List<Span> doFlexibleTraceQuery(String traceId) throws IOException {
		return candidates.get(config.getRecord()).doFlexibleTraceQuery(traceId);
	}

}
