package org.storage.prometheus.plugin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

import org.apache.skywalking.oap.server.core.analysis.DownSampling;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.CustomCollectorRegistry;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.exporter.HTTPServer;
import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

	static {
		try {
			new HTTPServer(new InetSocketAddress(8119), CustomCollectorRegistry.defaultRegistry);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		int i = 0;
		while(i < 120) {
			i++;
			final int value = i;
			new Collector() {
				@Override
				public List<MetricFamilySamples> collect() {
					long ts = System.currentTimeMillis();
					String id = "prometheus_api_0";
					long timebucket = TimeBucket.getTimeBucket(ts, DownSampling.Minute);
					long timestamp = TimeBucket.getTimestamp(timebucket, DownSampling.Minute);
					System.out.println("timebucket:" + timebucket + " timestamp:" + timestamp + " value:" + value);
					return Collections.singletonList(new MetricFamilySamples("custom_collector_test_final_3", Type.GAUGE,
							"custom_collector_test_final_3 help", Collections.singletonList(new Sample("custom_collector_test_final_3",
									Collections.singletonList("id"), Collections.singletonList(id), value, timestamp))));
				}
			}.register(CustomCollectorRegistry.defaultRegistry);
			Thread.sleep(1000l);
		}
	}
}
