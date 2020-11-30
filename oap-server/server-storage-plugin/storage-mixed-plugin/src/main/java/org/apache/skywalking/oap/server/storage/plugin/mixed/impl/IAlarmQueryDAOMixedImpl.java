package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.skywalking.oap.server.core.query.type.Alarms;
import org.apache.skywalking.oap.server.core.storage.query.IAlarmQueryDAO;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IAlarmQueryDAOMixedImpl implements IAlarmQueryDAO {

	private final StorageModuleMixedConfig config;
	
	private final Map<String/*provider*/, IAlarmQueryDAO> candidates;
	
	@Override
	public Alarms getAlarm(Integer scopeId, String keyword, int limit, int from, long startTB, long endTB)
			throws IOException {
		return candidates.get(config.getRecord()).getAlarm(scopeId, keyword, limit, from, startTB, endTB);
	}

}
