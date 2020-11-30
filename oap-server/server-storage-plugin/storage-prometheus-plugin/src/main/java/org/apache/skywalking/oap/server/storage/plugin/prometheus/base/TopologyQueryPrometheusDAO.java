package org.apache.skywalking.oap.server.storage.plugin.prometheus.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.skywalking.oap.server.core.analysis.manual.relation.service.ServiceRelationServerSideMetrics;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.query.type.Call;
import org.apache.skywalking.oap.server.core.query.type.Call.CallDetail;
import org.apache.skywalking.oap.server.core.source.DetectPoint;
import org.apache.skywalking.oap.server.core.storage.query.ITopologyQueryDAO;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TopologyQueryPrometheusDAO implements ITopologyQueryDAO {
	
	private final PrometheusHttpApi api;

	@Override
	public List<CallDetail> loadServiceRelationsDetectedAtServerSide(long startTB, long endTB, List<String> serviceIds)
			throws IOException {
		// TODO Auto-generated method stub
		
		api.rangeQuery("", null, startTB, endTB);
		
		return null;
	}
	
	private List<Call.CallDetail> buildServiceRelation(SearchSourceBuilder sourceBuilder, String indexName,
            DetectPoint detectPoint) throws IOException {
		sourceBuilder.aggregation(
		AggregationBuilders
			.terms(Metrics.ENTITY_ID).field(Metrics.ENTITY_ID)
			.subAggregation(
					AggregationBuilders.terms(ServiceRelationServerSideMetrics.COMPONENT_ID)
						.field(ServiceRelationServerSideMetrics.COMPONENT_ID))
			.size(1000));
		
		SearchResponse response = null;
		
		List<Call.CallDetail> calls = new ArrayList<>();
		Terms entityTerms = response.getAggregations().get(Metrics.ENTITY_ID);
		for (Terms.Bucket entityBucket : entityTerms.getBuckets()) {
			String entityId = entityBucket.getKeyAsString();
			Terms componentTerms = entityBucket.getAggregations().get(ServiceRelationServerSideMetrics.COMPONENT_ID);
			final int componentId = componentTerms.getBuckets().get(0).getKeyAsNumber().intValue();
			
			Call.CallDetail call = new Call.CallDetail();
			call.buildFromServiceRelation(entityId, componentId, detectPoint);
			calls.add(call);
		}
		return calls;
	}

	@Override
	public List<CallDetail> loadServiceRelationDetectedAtClientSide(long startTB, long endTB, List<String> serviceIds)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CallDetail> loadServiceRelationsDetectedAtServerSide(long startTB, long endTB) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CallDetail> loadServiceRelationDetectedAtClientSide(long startTB, long endTB) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CallDetail> loadInstanceRelationDetectedAtServerSide(String clientServiceId, String serverServiceId,
			long startTB, long endTB) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CallDetail> loadInstanceRelationDetectedAtClientSide(String clientServiceId, String serverServiceId,
			long startTB, long endTB) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CallDetail> loadEndpointRelation(long startTB, long endTB, String destEndpointId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
