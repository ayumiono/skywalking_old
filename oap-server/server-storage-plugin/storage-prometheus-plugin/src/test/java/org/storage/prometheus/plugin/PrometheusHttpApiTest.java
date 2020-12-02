package org.storage.prometheus.plugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.skywalking.oap.server.core.analysis.DownSampling;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.networkalias.NetworkAddressAlias;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi.PrometheusHttpAPIRespBody;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrometheusHttpApiTest {

	private PrometheusHttpApi api = new PrometheusHttpApi("http://192.168.201.31:9090");
	
	@Test
	public void query() {
		
		Model model = new Model("custom_collector_test_final", null, null, 1, DownSampling.Minute, false, false, null);
		
		List<String> ids = new ArrayList<String>();
		
		ids.add("202012021151_prometheus_api_0");
		ids.add("202012021152_prometheus_api_0");
		
		PrometheusHttpAPIRespBody response = api.query(model, ids);
	
		log.info(response+"");
	}
	
	@Test
	public void rangeQuery() {
		
		long start = TimeBucket.getMinuteTimeBucket(System.currentTimeMillis() - 60_000L * 10);
		
		PrometheusHttpAPIRespBody response = api.rangeQuery(NetworkAddressAlias.INDEX_NAME, null, TimeBucket.getTimestamp(start, DownSampling.Minute));
	
		log.info(response+"");
	}
	
}
