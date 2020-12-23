package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.query.input.Duration;
import org.apache.skywalking.oap.server.core.query.input.TopNCondition;
import org.apache.skywalking.oap.server.core.query.type.KeyValue;
import org.apache.skywalking.oap.server.core.query.type.SelectedRecord;
import org.apache.skywalking.oap.server.core.storage.query.IAggregationQueryDAO;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IAggregationQueryDAOMixedImpl implements IAggregationQueryDAO {
	
	private final StorageModuleMixedConfig config;
	
	private final Map<String/*provider*/, IAggregationQueryDAO> candidates;

	@Override
	public List<SelectedRecord> sortMetrics(TopNCondition condition, String valueColumnName, Duration duration,
			List<KeyValue> additionalConditions) throws IOException {
		return candidates.get(config.getMetrics()).sortMetrics(condition, valueColumnName, duration, additionalConditions);
	}

}
