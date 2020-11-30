package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.query.input.Duration;
import org.apache.skywalking.oap.server.core.query.input.MetricsCondition;
import org.apache.skywalking.oap.server.core.query.type.HeatMap;
import org.apache.skywalking.oap.server.core.query.type.MetricsValues;
import org.apache.skywalking.oap.server.core.storage.query.IMetricsQueryDAO;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IMetricsQueryDAOMixedImpl implements IMetricsQueryDAO {

	private final StorageModuleMixedConfig config;
	
	private final Map<String/*provider*/, IMetricsQueryDAO> candidates;
	
	@Override
	public int readMetricsValue(MetricsCondition condition, String valueColumnName, Duration duration)
			throws IOException {
		return candidates.get(config.getMetrics()).readMetricsValue(condition, valueColumnName, duration);
	}

	@Override
	public MetricsValues readMetricsValues(MetricsCondition condition, String valueColumnName, Duration duration)
			throws IOException {
		return candidates.get(config.getMetrics()).readMetricsValues(condition, valueColumnName, duration);
	}

	@Override
	public List<MetricsValues> readLabeledMetricsValues(MetricsCondition condition, String valueColumnName,
			List<String> labels, Duration duration) throws IOException {
		return candidates.get(config.getMetrics()).readLabeledMetricsValues(condition, valueColumnName, labels, duration);
	}

	@Override
	public HeatMap readHeatMap(MetricsCondition condition, String valueColumnName, Duration duration)
			throws IOException {
		return candidates.get(config.getMetrics()).readHeatMap(condition, valueColumnName, duration);
	}

}
