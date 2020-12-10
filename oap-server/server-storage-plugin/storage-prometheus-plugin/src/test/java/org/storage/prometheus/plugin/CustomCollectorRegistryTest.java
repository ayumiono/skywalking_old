package org.storage.prometheus.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.CustomCollectorRegistry;
import org.junit.Test;

import com.google.gson.Gson;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomCollectorRegistryTest {
	
	private Gson gson = new Gson();
	
	@Test
	public void test() throws InterruptedException {
		Map<String, String> labels = new HashMap<>();
		labels.put("service_id", "asdfa_x121dfd");
		int i = 0;
		while(true) {
			final int _i = i++;
			
			new Collector() {
				@Override
				public List<MetricFamilySamples> collect() {
					return Collections.singletonList(new MetricFamilySamples("xuelong_chen", Type.GAUGE, "", Collections.singletonList(new Sample("xuelong_chen", new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), _i, 1607070780000L))));
				}
			}.register(CustomCollectorRegistry.defaultRegistry);
			
			Enumeration<Collector.MetricFamilySamples> mfs = CustomCollectorRegistry.defaultRegistry.filteredMetricFamilySamples(new HashSet<String>());
			while(mfs.hasMoreElements()) {
			      Collector.MetricFamilySamples metricFamilySamples = mfs.nextElement();
			      System.out.println(gson.toJson(metricFamilySamples.samples.get(0).value));
			}
			
			Thread.sleep(1000L);
		}
		
	}
	
}
