package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.core.query.type.Call.CallDetail;
import org.apache.skywalking.oap.server.core.storage.query.ITopologyQueryDAO;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ITopologyQueryDAOMixedImpl implements ITopologyQueryDAO {
	
	private final StorageModuleMixedConfig config;
	
	private final Map<String/*provider*/, ITopologyQueryDAO> candidates;

	@Override
	public List<CallDetail> loadServiceRelationsDetectedAtServerSide(long startTB, long endTB, List<String> serviceIds)
			throws IOException {
		return candidates.get(config.getMetrics()).loadServiceRelationsDetectedAtServerSide(startTB, endTB);
	}

	@Override
	public List<CallDetail> loadServiceRelationDetectedAtClientSide(long startTB, long endTB, List<String> serviceIds)
			throws IOException {
		return candidates.get(config.getMetrics()).loadServiceRelationDetectedAtClientSide(startTB, endTB);
	}

	@Override
	public List<CallDetail> loadServiceRelationsDetectedAtServerSide(long startTB, long endTB) throws IOException {
		return candidates.get(config.getMetrics()).loadServiceRelationsDetectedAtServerSide(startTB, endTB);
	}

	@Override
	public List<CallDetail> loadServiceRelationDetectedAtClientSide(long startTB, long endTB) throws IOException {
		return candidates.get(config.getMetrics()).loadServiceRelationDetectedAtClientSide(startTB, endTB);
	}

	@Override
	public List<CallDetail> loadInstanceRelationDetectedAtServerSide(String clientServiceId, String serverServiceId,
			long startTB, long endTB) throws IOException {
		return candidates.get(config.getMetrics()).loadInstanceRelationDetectedAtServerSide(clientServiceId, serverServiceId, startTB, endTB);
	}

	@Override
	public List<CallDetail> loadInstanceRelationDetectedAtClientSide(String clientServiceId, String serverServiceId,
			long startTB, long endTB) throws IOException {
		return candidates.get(config.getMetrics()).loadInstanceRelationDetectedAtClientSide(clientServiceId, serverServiceId, startTB, endTB);
	}

	@Override
	public List<CallDetail> loadEndpointRelation(long startTB, long endTB, String destEndpointId) throws IOException {
		return candidates.get(config.getMetrics()).loadEndpointRelation(startTB, endTB, destEndpointId);
	}

}
