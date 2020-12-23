package org.apache.skywalking.oap.server.receiver.trace.provider.parser.listener;

import java.util.List;

import org.apache.skywalking.oap.server.analyzer.provider.trace.parser.IGroupParserService;
import org.apache.skywalking.oap.server.core.source.Endpoint;

public class MockGroupParser implements IGroupParserService {
	@Override
	public List<String> group(Endpoint endpoint) {
		return null;
	}
}
