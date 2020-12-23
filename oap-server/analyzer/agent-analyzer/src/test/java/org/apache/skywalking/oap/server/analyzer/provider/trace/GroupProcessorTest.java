package org.apache.skywalking.oap.server.analyzer.provider.trace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.analyzer.provider.AnalyzerModuleConfig;
import org.apache.skywalking.oap.server.analyzer.provider.trace.parser.listener.GroupProcessor;
import org.apache.skywalking.oap.server.core.source.Endpoint;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class GroupProcessorTest {

	GroupProcessor processor;
	
	@Before
    public void init() throws FileNotFoundException {
		AnalyzerModuleConfig config = new AnalyzerModuleConfig();
		Yaml yaml = new Yaml();
		Map<String, Map<String, Object>> map = yaml.loadAs(new FileReader(new File("D:\\Users\\Desktop\\groupRules.yml")), Map.class);
		List<Map<String, Object>> groupRules = (List<Map<String, Object>>) map.get("groupRules");
		processor = new GroupProcessor(config);
    }
	
	@Test
	public void groupLabels() {
		Endpoint endpoint = new Endpoint();
		endpoint.setServiceName("apm-test-provider");
		endpoint.setName("me.ayumi.prometheus.demo.Interface0.id()");
		System.out.println(processor.group(endpoint));
		endpoint.setName("me.ayumi.prometheus.demo.Interface1.id()");
		System.out.println(processor.group(endpoint));
		endpoint.setName("me.ayumi.prometheus.demo.Interface2.id()");
		System.out.println(processor.group(endpoint));
	}
	
}
