package org.apache.skywalking.oap.server.analyzer.provider.trace.parser.listener;

import java.util.ArrayList;
import java.util.List;

import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.apache.skywalking.apm.network.language.agent.v3.SpanObject;
import org.apache.skywalking.oap.server.analyzer.module.AnalyzerModule;
import org.apache.skywalking.oap.server.analyzer.provider.AnalyzerModuleConfig;
import org.apache.skywalking.oap.server.analyzer.provider.trace.parser.IGroupParserService;
import org.apache.skywalking.oap.server.analyzer.provider.trace.parser.SpanTags;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.analysis.NodeType;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.config.NamingControl;
import org.apache.skywalking.oap.server.core.source.DetectPoint;
import org.apache.skywalking.oap.server.core.source.Endpoint;
import org.apache.skywalking.oap.server.core.source.EndpointGroup;
import org.apache.skywalking.oap.server.core.source.RequestType;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * add by xuelong.chen
 * 
 * @author Administrator
 */
@Slf4j
@RequiredArgsConstructor
public class GroupAnalysisListener implements EntryAnalysisListener {

	private final List<SourceBuilder> entrySourceBuilders = new ArrayList<>(10);
	private final SourceReceiver sourceReceiver;
	private final NamingControl namingControl;
	private final IGroupParserService groupParserService;

	@Override
	public boolean containsPoint(Point point) {
		return Point.Entry.equals(point);
	}

	/**
	 * only need endpoint info, so ignore source properties
	 */
	@Override
	public void parseEntry(SpanObject span, SegmentObject segmentObject) {
		log.info("GroupAnalysisListener parseEntry: {}", span);
		SourceBuilder sourceBuilder = new SourceBuilder(namingControl);
		sourceBuilder.setDestEndpointName(span.getOperationName());
		sourceBuilder.setDestServiceInstanceName(segmentObject.getServiceInstance());
		sourceBuilder.setDestServiceName(segmentObject.getService());
		sourceBuilder.setDestNodeType(NodeType.Normal);
		sourceBuilder.setDetectPoint(DetectPoint.SERVER);
		sourceBuilder.setComponentId(span.getComponentId());
		setPublicAttrs(sourceBuilder, span);
		entrySourceBuilders.add(sourceBuilder);
	}

	private EndpointGroup buildEndpointGroup(SourceBuilder entrySourceBuilder, String groupLabel) {
		log.info("GroupAnalysisListener buildEndpointGroup: {}", groupLabel);
		EndpointGroup endpointGroup = new EndpointGroup();
		endpointGroup.setName(entrySourceBuilder.getDestEndpointName());
		endpointGroup.setServiceName(entrySourceBuilder.getDestServiceName());
		endpointGroup.setServiceNodeType(entrySourceBuilder.getDestNodeType());
		endpointGroup.setServiceInstanceName(entrySourceBuilder.getDestServiceInstanceName());
		endpointGroup.setLatency(entrySourceBuilder.getLatency());
		endpointGroup.setStatus(entrySourceBuilder.isStatus());
		endpointGroup.setResponseCode(entrySourceBuilder.getResponseCode());
		endpointGroup.setType(entrySourceBuilder.getType());
		endpointGroup.setTimeBucket(entrySourceBuilder.getTimeBucket());
		endpointGroup.setGroup(groupLabel);
//		endpointGroup.prepare();
//		log.info("EndpointGroup id: {}", endpointGroup.getEntityId());
		return endpointGroup;
	}

	@Override
	public void build() {
		entrySourceBuilders.forEach(entrySourceBuilder -> {
			Endpoint endpoint = entrySourceBuilder.toEndpoint();
			List<String> groupLabels = this.groupParserService.group(endpoint);
			groupLabels.forEach(label->{
				sourceReceiver.receive(buildEndpointGroup(entrySourceBuilder, label));
			});
		});
	}

	private void setPublicAttrs(SourceBuilder sourceBuilder, SpanObject span) {
		long latency = span.getEndTime() - span.getStartTime();
		sourceBuilder.setTimeBucket(TimeBucket.getMinuteTimeBucket(span.getStartTime()));
		sourceBuilder.setLatency((int) latency);
		sourceBuilder.setResponseCode(Const.NONE);
		span.getTagsList().forEach(tag -> {
			if (SpanTags.STATUS_CODE.equals(tag.getKey())) {
				try {
					sourceBuilder.setResponseCode(Integer.parseInt(tag.getValue()));
				} catch (NumberFormatException e) {
					log.warn("span {} has illegal status code {}", span, tag.getValue());
				}
			}
		});

		sourceBuilder.setStatus(!span.getIsError());

		switch (span.getSpanLayer()) {
		case Http:
			sourceBuilder.setType(RequestType.HTTP);
			break;
		case Database:
			sourceBuilder.setType(RequestType.DATABASE);
			break;
		default:
			sourceBuilder.setType(RequestType.RPC);
			break;
		}
	}

	public static class Factory implements AnalysisListenerFactory {
		private final SourceReceiver sourceReceiver;
		private final NamingControl namingControl;
		private final IGroupParserService groupParserService;

		public Factory(ModuleManager moduleManager) {
			this.sourceReceiver = moduleManager.find(CoreModule.NAME).provider().getService(SourceReceiver.class);
			this.namingControl = moduleManager.find(CoreModule.NAME).provider().getService(NamingControl.class);
			this.groupParserService = moduleManager.find(AnalyzerModule.NAME).provider().getService(IGroupParserService.class);
		}

		@Override
		public AnalysisListener create(ModuleManager moduleManager, AnalyzerModuleConfig config) {
			return new GroupAnalysisListener(sourceReceiver, namingControl, groupParserService);
		}
	}
}
