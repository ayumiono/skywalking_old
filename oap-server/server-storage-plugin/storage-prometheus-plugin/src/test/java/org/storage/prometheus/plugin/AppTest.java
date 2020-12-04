package org.storage.prometheus.plugin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;
import org.apache.skywalking.oap.server.core.analysis.metrics.DataTable;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.CustomCollectorRegistry;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.HTTPServer;
import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

	static Histogram histogram = null;
	static final Summary myMetric = Summary.build()
			 .quantile(0.5, 0.05)   // Add 50th percentile (= median) with 5% tolerated error
			 .quantile(0.9, 0.01)   // Add 90th percentile with 1% tolerated error
			 .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
			 .name("requests_size_bytes")
			 .help("Request size in bytes.")
			 .register();
	static {
		try {
//			new HTTPServer(new InetSocketAddress(8119), CustomCollectorRegistry.defaultRegistry);
			new HTTPServer(8119);
			histogram = Histogram.build().name("histogram_test").help("histogram help").labelNames("id").register();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static final int[] RANKS = {
	        50,
	        75,
	        90,
	        95,
	        99
	    };

	public static void main(String[] args) throws InterruptedException {
		
		while(true) {
			myMetric.observe(RandomUtils.nextDouble(0.0, 10));
			Thread.sleep(RandomUtils.nextInt(10, 1000));
		}
		
//		while(true) {
//			histogram.labels("00001-id").time(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						Thread.sleep(RandomUtils.nextLong(5, 10000));
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			});
//		}
//		int i = 0;
//		String name = "custom_summary_metrics_test";
//		Map<String, String> labels = new HashMap<>();
//		labels.put("service_id", "asdfa_x121dfd");
//		long timestamp = System.currentTimeMillis();
//		DataTable dataSet = new DataTable();
//		dataSet.put("0.025", 0l);
//		dataSet.put("0.05", 0l);
//		dataSet.put("0.075", 0l);
//		dataSet.put("0.1", 1l);
//		dataSet.put("0.25", 1l);
//		dataSet.put("0.5", 2l);
//		dataSet.put("0.75", 4l);
//		dataSet.put("1.0", 5l);
//		dataSet.put("10.0", 41l);
//		dataSet.put("2.5", 13l);
//		dataSet.put("5.0", 21l);
//		dataSet.put("7.5", 31l);
//		DataTable percentileValues = new DataTable();
//		percentileValues.put("0", 51l);
//		percentileValues.put("1", 41l);
//		percentileValues.put("2", 31l);
//		percentileValues.put("3", 21l);
//		percentileValues.put("4", 1l);
//		while(i < 120) {
//			i++;
//			final int value = i;
//			new Collector() {
//				@Override
//				public List<MetricFamilySamples> collect() {
//					List<Sample> samples = new ArrayList<>();
//					Sample sumSample = new Sample(name+"_sum", new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
//							dataSet.sumOfValues(), timestamp);
//					Sample countSample = new Sample(name + "_count", new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), 
//							dataSet.size(), timestamp);
//					samples.add(sumSample);samples.add(countSample);
//					percentileValues.keys().forEach(key->{
//						long value = percentileValues.get(key);
//						labels.put("quantile", "p" + RANKS[Integer.parseInt(key)]);
//						Sample sample = new Sample(name, new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), value, timestamp);
//						samples.add(sample);
//					});
//					dataSet.keys().forEach(key->{
//						long value = dataSet.get(key);
//						labels.put("le", key);
//						Sample sample = new Sample(name+"_bucket", new ArrayList<>(labels.keySet()), new ArrayList<>(labels.values()), value, timestamp);
//						samples.add(sample);
//					});
//					return Collections.singletonList(new MetricFamilySamples(name, Type.SUMMARY, "", samples));
//				}
//			}.register(CustomCollectorRegistry.defaultRegistry);
//			Thread.sleep(1000l);
//		}
	}
}
