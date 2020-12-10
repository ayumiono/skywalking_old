package org.storage.prometheus.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.HTTPServer;
import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	
	private static PrometheusHttpApi api = new PrometheusHttpApi("http://192.168.201.31:9090");

	static Histogram histogram = Histogram.build().name("histogram_test").help("histogram help").labelNames("id").register();
	static final Summary myMetric = Summary.build()
			 .quantile(0.5, 0.05)   // Add 50th percentile (= median) with 5% tolerated error
			 .quantile(0.9, 0.01)   // Add 90th percentile with 1% tolerated error
			 .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
			 .name("requests_size_bytes")
			 .help("Request size in bytes.")
			 .register();
	static final Gauge gauge = Gauge.build().name("gauge_test").help("gauge help").labelNames("id").register();
	static {
		try {
			new HTTPServer(8119);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
//		while(true) {
//			gauge.labels("asdfa_x121dfd").inc();
//			Thread.sleep(5000);
//		}
//		long timestapm = System.currentTimeMillis();
		long timestapm = 1607513370021L;
		System.out.println("timestamp:"+timestapm);
		Map<String, String> labels = new HashMap<>();
		labels.put("service_id", "asdfa_x121dfd");
		labels.put("age", "1");
//		new Collector() {
//			@Override
//			public List<MetricFamilySamples> collect() {
//				return Collections.singletonList(new MetricFamilySamples("updatable_test", Type.GAUGE, "", Collections.singletonList(new Sample("updatable_test", new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 13, timestapm))));
//			}
//		}.register();
//		Thread.sleep(30000);
		labels.put("age", "2");
//		CollectorRegistry.defaultRegistry.clear();
		new Collector() {
			@Override
			public List<MetricFamilySamples> collect() {
				return Collections.singletonList(new MetricFamilySamples("updatable_test", Type.GAUGE, "", 
						Collections.singletonList(new Sample("updatable_test", new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
								2, timestapm))));
			}
		}.register();
	}
}
