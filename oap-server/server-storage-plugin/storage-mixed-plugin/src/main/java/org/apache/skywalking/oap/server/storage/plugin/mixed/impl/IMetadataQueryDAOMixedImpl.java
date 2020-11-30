package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.query.type.Database;
import org.apache.skywalking.oap.server.core.query.type.Endpoint;
import org.apache.skywalking.oap.server.core.query.type.Service;
import org.apache.skywalking.oap.server.core.query.type.ServiceInstance;
import org.apache.skywalking.oap.server.core.storage.query.IMetadataQueryDAO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IMetadataQueryDAOMixedImpl implements IMetadataQueryDAO {
	
	final Map<String, IMetadataQueryDAO> candidates;

	@Override
	public List<Service> getAllServices(long startTimestamp, long endTimestamp) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Service> getAllBrowserServices(long startTimestamp, long endTimestamp) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Database> getAllDatabases() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Service> searchServices(long startTimestamp, long endTimestamp, String keyword) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Service searchService(String serviceCode) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Endpoint> searchEndpoint(String keyword, String serviceId, int limit) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ServiceInstance> getServiceInstances(long startTimestamp, long endTimestamp, String serviceId)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
