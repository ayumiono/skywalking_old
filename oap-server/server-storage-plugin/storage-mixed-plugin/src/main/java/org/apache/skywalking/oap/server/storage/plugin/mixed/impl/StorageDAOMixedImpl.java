package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.library.client.request.UpdateRequest;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StorageDAOMixedImpl implements StorageDAO {
	
	private final static Logger logger = LoggerFactory.getLogger(StorageDAOMixedImpl.class);
	
	private final Map<String/*provider*/, StorageDAO> candidates;
	
	private final StorageModuleMixedConfig config;

	@Override
	public IMetricsDAO newMetricsDao(StorageBuilder<Metrics> storageBuilder) {
		Map<String, IMetricsDAO> _candidates = new HashMap<>();
		for(Entry<String, StorageDAO> candidate : candidates.entrySet()) {
			try {
				_candidates.put(candidate.getKey(), candidate.getValue().newMetricsDao(storageBuilder));
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			
		}
		return new IMetricsDAOMixedImpl(_candidates);
	}

	@Override
	public IRecordDAO newRecordDao(StorageBuilder<Record> storageBuilder) {
		Map<String, IRecordDAO> _candidates = new HashMap<>();
		for(Entry<String, StorageDAO> candidate : candidates.entrySet()) {
			try {
				_candidates.put(candidate.getKey(), candidate.getValue().newRecordDao(storageBuilder));
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		return new IRecordDAOMixedImpl(_candidates);
	}

	@Override
	public INoneStreamDAO newNoneStreamDao(StorageBuilder<NoneStream> storageBuilder) {
		Map<String, INoneStreamDAO> _candidates = new HashMap<>();
		for(Entry<String, StorageDAO> candidate : candidates.entrySet()) {
			try {
				_candidates.put(candidate.getKey(), candidate.getValue().newNoneStreamDao(storageBuilder));
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		return new INoneStreamDAOMixedImpl(_candidates);
	}

	@Override
	public IManagementDAO newManagementDao(StorageBuilder<ManagementData> storageBuilder) {
		Map<String, IManagementDAO> _candidates = new HashMap<>();
		for(Entry<String, StorageDAO> candidate : candidates.entrySet()) {
			try {
				_candidates.put(candidate.getKey(), candidate.getValue().newManagementDao(storageBuilder));
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		return new IManagementDAOMixedImpl(_candidates);
	}
	
	@RequiredArgsConstructor
	public class IManagementDAOMixedImpl implements IManagementDAO {
		final Map<String, IManagementDAO> candidates;

		@Override
		public void insert(Model model, ManagementData storageData) throws IOException {
			candidates.get(config.getManagement()).insert(model, storageData);
		}
	}
	
	@RequiredArgsConstructor
	public class INoneStreamDAOMixedImpl implements INoneStreamDAO {
		final Map<String, INoneStreamDAO> candidates;

		@Override
		public void insert(Model model, NoneStream noneStream) throws IOException {
			candidates.get(config.getNone()).insert(model, noneStream);
		}
	}
	
	@RequiredArgsConstructor
	public class IRecordDAOMixedImpl implements IRecordDAO {
		final Map<String, IRecordDAO> candidates;

		@Override
		public InsertRequest prepareBatchInsert(Model model, Record record) throws IOException {
			return candidates.get(config.getRecord()).prepareBatchInsert(model, record);
		}
	}
	
	@RequiredArgsConstructor
	public class IMetricsDAOMixedImpl implements IMetricsDAO {
		
		final Map<String, IMetricsDAO> candidates;

		@Override
		public List<Metrics> multiGet(Model model, List<String> ids) throws IOException {
			return candidates.get(config.getMetrics()).multiGet(model, ids);
		}

		@Override
		public InsertRequest prepareBatchInsert(Model model, Metrics metrics) throws IOException {
			return candidates.get(config.getMetrics()).prepareBatchInsert(model, metrics);
		}

		@Override
		public UpdateRequest prepareBatchUpdate(Model model, Metrics metrics) throws IOException {
			return candidates.get(config.getMetrics()).prepareBatchUpdate(model, metrics);
		}
	}

}
