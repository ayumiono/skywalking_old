package org.apache.skywalking.oap.server.storage.plugin.prometheus.base;

import java.io.IOException;
import java.util.List;

import org.apache.skywalking.oap.server.core.query.input.Duration;
import org.apache.skywalking.oap.server.core.query.input.MetricsCondition;
import org.apache.skywalking.oap.server.core.query.type.HeatMap;
import org.apache.skywalking.oap.server.core.query.type.MetricsValues;
import org.apache.skywalking.oap.server.core.storage.query.IMetricsQueryDAO;

public class MetricsQueryPrometheusDAO implements IMetricsQueryDAO {

	@Override
	public int readMetricsValue(MetricsCondition condition, String valueColumnName, Duration duration)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public MetricsValues readMetricsValues(MetricsCondition condition, String valueColumnName, Duration duration)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MetricsValues> readLabeledMetricsValues(MetricsCondition condition, String valueColumnName,
			List<String> labels, Duration duration) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HeatMap readHeatMap(MetricsCondition condition, String valueColumnName, Duration duration)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
