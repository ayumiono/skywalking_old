package org.apache.skywalking.oal.rt.parser;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;

import org.apache.skywalking.oal.rt.OALRuntime;
import org.apache.skywalking.oap.server.core.annotation.AnnotationScan;
import org.apache.skywalking.oap.server.core.source.DefaultScopeDefine;
import org.apache.skywalking.oap.server.core.storage.StorageException;
import org.junit.Before;
import org.junit.Test;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import freemarker.template.Version;

public class OALRuntimeTest {

	private static final String TEST_SOURCE_PACKAGE = ScriptParserTest.class.getPackage().getName() + ".test.source.";
	
	Configuration configuration;

	@Before
	public void init() throws IOException, StorageException {
		configuration = new Configuration(new Version("2.3.28"));
		configuration.setEncoding(Locale.ENGLISH, "UTF-8");
		configuration.setClassLoaderForTemplateLoading(OALRuntime.class.getClassLoader(), "/code-templates");
	
		AnnotationScan scopeScan = new AnnotationScan();
		scopeScan.registerListener(new DefaultScopeDefine.Listener());
		scopeScan.scan();
		MetricsHolder.init();
	}

	@Test
	public void getMeta() throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, TemplateException, IOException {
		StringWriter methodEntity = new StringWriter();
		ScriptParser parser = ScriptParser.createFromScriptText(
				"endpoint_group_avg = from(EndpointGroup.latency).longAvg();", TEST_SOURCE_PACKAGE);
		List<AnalysisResult> results = parser.parse().getMetricsStmts();
		configuration.getTemplate("metrics/getMeta.ftl").process(results.get(0), methodEntity);
		System.out.println(methodEntity);
	}
}
