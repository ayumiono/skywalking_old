package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.skywalking.oap.server.core.storage.IHistoryDeleteDAO;
import org.apache.skywalking.oap.server.core.storage.model.Model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IHistoryDeleteDAOMixedImpl implements IHistoryDeleteDAO {

	private final Map<String, IHistoryDeleteDAO> candidates;
	
	@Override
	public void deleteHistory(Model model, String timeBucketColumnName, int ttl) throws IOException {
		for (IHistoryDeleteDAO candidate : candidates.values()) {
			candidate.deleteHistory(model, timeBucketColumnName, ttl);
		}
	}

}
