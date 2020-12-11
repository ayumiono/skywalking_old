package org.apache.skywalking.oap.server.storage.plugin.prometheus.base;

import org.apache.skywalking.oap.server.core.analysis.config.NoneStream;
import org.apache.skywalking.oap.server.core.analysis.management.ManagementData;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.analysis.record.Record;
import org.apache.skywalking.oap.server.core.storage.IManagementDAO;
import org.apache.skywalking.oap.server.core.storage.IMetricsDAO;
import org.apache.skywalking.oap.server.core.storage.INoneStreamDAO;
import org.apache.skywalking.oap.server.core.storage.IRecordDAO;
import org.apache.skywalking.oap.server.core.storage.StorageBuilder;
import org.apache.skywalking.oap.server.core.storage.StorageDAO;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.StorageModulePrometheusConfig;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PromeMetric;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StoragePrometheusDao implements StorageDAO {
	
	protected final StorageModulePrometheusConfig config;
	
	protected final PrometheusMeterMapper<Metrics, PromeMetric> mapper;
	
	@Override
	public IMetricsDAO newMetricsDao(StorageBuilder<Metrics> storageBuilder) {
		return new MetricsPrometheusDAO(new PrometheusHttpApi(config.getPrometheusAddress()), mapper);
	}

	@Override
	public IRecordDAO newRecordDao(StorageBuilder<Record> storageBuilder) {
		throw new UnsupportedOperationException("prometheus只用来存储指标数据");
	}

	@Override
	public INoneStreamDAO newNoneStreamDao(StorageBuilder<NoneStream> storageBuilder) {
		throw new UnsupportedOperationException("prometheus只用来存储指标数据");
	}

	@Override
	public IManagementDAO newManagementDao(StorageBuilder<ManagementData> storageBuilder) {
		throw new UnsupportedOperationException("prometheus只用来存储指标数据");
	}

}
