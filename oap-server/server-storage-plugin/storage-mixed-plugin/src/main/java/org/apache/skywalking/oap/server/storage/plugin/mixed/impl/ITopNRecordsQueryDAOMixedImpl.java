package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.query.input.Duration;
import org.apache.skywalking.oap.server.core.query.input.TopNCondition;
import org.apache.skywalking.oap.server.core.query.type.SelectedRecord;
import org.apache.skywalking.oap.server.core.storage.query.ITopNRecordsQueryDAO;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ITopNRecordsQueryDAOMixedImpl implements ITopNRecordsQueryDAO {
	
	private final StorageModuleMixedConfig config;
	
	private final Map<String/*provider*/, ITopNRecordsQueryDAO> candidates;

	@Override
	public List<SelectedRecord> readSampledRecords(TopNCondition condition, String valueColumnName, Duration duration)
			throws IOException {
		return candidates.get(config.getRecord()).readSampledRecords(condition, valueColumnName, duration);
	}

}
