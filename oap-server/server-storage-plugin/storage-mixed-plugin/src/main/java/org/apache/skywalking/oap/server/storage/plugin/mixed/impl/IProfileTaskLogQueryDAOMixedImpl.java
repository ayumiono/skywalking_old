package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.query.type.ProfileTaskLog;
import org.apache.skywalking.oap.server.core.storage.profile.IProfileTaskLogQueryDAO;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IProfileTaskLogQueryDAOMixedImpl implements IProfileTaskLogQueryDAO {

	private final StorageModuleMixedConfig config;

	private final Map<String/* provider */, IProfileTaskLogQueryDAO> candidates;

	@Override
	public List<ProfileTaskLog> getTaskLogList() throws IOException {
		return candidates.get(config.getRecord()).getTaskLogList();
	}

}
