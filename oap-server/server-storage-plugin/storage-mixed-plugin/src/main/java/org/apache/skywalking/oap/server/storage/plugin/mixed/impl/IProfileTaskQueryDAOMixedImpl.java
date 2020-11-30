package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.query.type.ProfileTask;
import org.apache.skywalking.oap.server.core.storage.profile.IProfileTaskQueryDAO;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IProfileTaskQueryDAOMixedImpl implements IProfileTaskQueryDAO {

	private final StorageModuleMixedConfig config;
	
	private final Map<String/*provider*/, IProfileTaskQueryDAO> candidates;
	
	@Override
	public List<ProfileTask> getTaskList(String serviceId, String endpointName, Long startTimeBucket,
			Long endTimeBucket, Integer limit) throws IOException {
		return candidates.get(config.getNone()).getTaskList(serviceId, endpointName, startTimeBucket, endTimeBucket, limit);
	}

	@Override
	public ProfileTask getById(String id) throws IOException {
		return candidates.get(config.getNone()).getById(id);
	}

}
