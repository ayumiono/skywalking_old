package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.query.type.Database;
import org.apache.skywalking.oap.server.core.query.type.Endpoint;
import org.apache.skywalking.oap.server.core.query.type.Service;
import org.apache.skywalking.oap.server.core.query.type.ServiceInstance;
import org.apache.skywalking.oap.server.core.storage.query.IMetadataQueryDAO;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IMetadataQueryDAOMixedImpl implements IMetadataQueryDAO {
	
	private final StorageModuleMixedConfig config;
	
	final Map<String, IMetadataQueryDAO> candidates;

	@Override
	public List<Service> getAllServices(long startTimestamp, long endTimestamp) throws IOException {
		return candidates.get(config.getMetrics()).getAllServices(startTimestamp, endTimestamp);
	}

	@Override
	public List<Service> getAllBrowserServices(long startTimestamp, long endTimestamp) throws IOException {
		return candidates.get(config.getMetrics()).getAllBrowserServices(startTimestamp, endTimestamp);
	}

	@Override
	public List<Database> getAllDatabases() throws IOException {
		return candidates.get(config.getMetrics()).getAllDatabases();
	}

	@Override
	public List<Service> searchServices(long startTimestamp, long endTimestamp, String keyword) throws IOException {
		return candidates.get(config.getMetrics()).searchServices(startTimestamp, endTimestamp, keyword);
	}

	@Override
	public Service searchService(String serviceCode) throws IOException {
		return candidates.get(config.getMetrics()).searchService(serviceCode);
	}

	@Override
	public List<Endpoint> searchEndpoint(String keyword, String serviceId, int limit) throws IOException {
		return candidates.get(config.getMetrics()).searchEndpoint(keyword, serviceId, limit);
	}

	@Override
	public List<ServiceInstance> getServiceInstances(long startTimestamp, long endTimestamp, String serviceId)
			throws IOException {
		return candidates.get(config.getMetrics()).getServiceInstances(startTimestamp, endTimestamp, serviceId);
	}

}
