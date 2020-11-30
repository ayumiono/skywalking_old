package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.List;

import org.apache.skywalking.oap.server.core.analysis.manual.segment.SegmentRecord;
import org.apache.skywalking.oap.server.core.profile.ProfileThreadSnapshotRecord;
import org.apache.skywalking.oap.server.core.query.type.BasicTrace;
import org.apache.skywalking.oap.server.core.storage.profile.IProfileThreadSnapshotQueryDAO;

public class IProfileThreadSnapshotQueryDAOImpl implements IProfileThreadSnapshotQueryDAO {

	@Override
	public List<BasicTrace> queryProfiledSegments(String taskId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int queryMinSequence(String segmentId, long start, long end) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryMaxSequence(String segmentId, long start, long end) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<ProfileThreadSnapshotRecord> queryRecords(String segmentId, int minSequence, int maxSequence)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SegmentRecord getProfiledSegment(String segmentId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
