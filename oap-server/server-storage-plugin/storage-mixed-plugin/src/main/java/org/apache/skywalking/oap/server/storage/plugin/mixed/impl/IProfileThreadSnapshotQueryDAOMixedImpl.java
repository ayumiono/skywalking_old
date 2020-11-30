package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.manual.segment.SegmentRecord;
import org.apache.skywalking.oap.server.core.profile.ProfileThreadSnapshotRecord;
import org.apache.skywalking.oap.server.core.query.type.BasicTrace;
import org.apache.skywalking.oap.server.core.storage.profile.IProfileThreadSnapshotQueryDAO;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IProfileThreadSnapshotQueryDAOMixedImpl implements IProfileThreadSnapshotQueryDAO {

	private final StorageModuleMixedConfig config;

	private final Map<String/* provider */, IProfileThreadSnapshotQueryDAO> candidates;
	
	@Override
	public List<BasicTrace> queryProfiledSegments(String taskId) throws IOException {
		return candidates.get(config.getRecord()).queryProfiledSegments(taskId);
	}

	@Override
	public int queryMinSequence(String segmentId, long start, long end) throws IOException {
		return candidates.get(config.getRecord()).queryMinSequence(segmentId, start, end);
	}

	@Override
	public int queryMaxSequence(String segmentId, long start, long end) throws IOException {
		return candidates.get(config.getRecord()).queryMaxSequence(segmentId, start, end);
	}

	@Override
	public List<ProfileThreadSnapshotRecord> queryRecords(String segmentId, int minSequence, int maxSequence)
			throws IOException {
		return candidates.get(config.getRecord()).queryRecords(segmentId, minSequence, maxSequence);
	}

	@Override
	public SegmentRecord getProfiledSegment(String segmentId) throws IOException {
		return candidates.get(config.getRecord()).getProfiledSegment(segmentId);
	}

}
