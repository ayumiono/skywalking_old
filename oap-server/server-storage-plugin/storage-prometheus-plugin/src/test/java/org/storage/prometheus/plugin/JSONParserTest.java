package org.storage.prometheus.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.skywalking.oap.server.core.analysis.DownSampling;
import org.apache.skywalking.oap.server.core.analysis.IDManager;
import org.apache.skywalking.oap.server.core.analysis.manual.networkalias.NetworkAddressAlias;
import org.apache.skywalking.oap.server.core.analysis.metrics.DoubleAvgMetrics;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.analysis.metrics.MetricsMetaInfo;
import org.apache.skywalking.oap.server.core.analysis.metrics.PercentileMetrics;
import org.apache.skywalking.oap.server.core.analysis.metrics.WithMetadata;
import org.apache.skywalking.oap.server.core.annotation.AnnotationListener;
import org.apache.skywalking.oap.server.core.annotation.AnnotationScan;
import org.apache.skywalking.oap.server.core.remote.grpc.proto.RemoteData;
import org.apache.skywalking.oap.server.core.remote.grpc.proto.RemoteData.Builder;
import org.apache.skywalking.oap.server.core.source.DefaultScopeDefine;
import org.apache.skywalking.oap.server.core.storage.StorageException;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.util.prometheus.metrics.Metric;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.DoubleAvgMetricsMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapperFacade;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMetricsMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.JSONParser;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.MetricFamily;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi.PrometheusHttpAPIRespBody;
import org.junit.Test;

import com.google.gson.Gson;

public class JSONParserTest {
	
	public static class PercentileMetricsMock extends PercentileMetrics implements WithMetadata {

		@Override
		public String id() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Builder serialize() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void deserialize(RemoteData remoteData) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public MetricsMetaInfo getMeta() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Metrics toHour() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Metrics toDay() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int remoteHashCode() {
			// TODO Auto-generated method stub
			return 0;
		}}
	
	public static class DoubleAvgMockMetrics extends DoubleAvgMetrics implements WithMetadata {
		
		@Override
		public String id() {
			return null;
		}

		@Override
		public Builder serialize() {
			return null;
		}

		@Override
		public void deserialize(RemoteData remoteData) {
		}

		@Override
		public MetricsMetaInfo getMeta() {
			return null;
		}

		@Override
		public Metrics toHour() {
			return null;
		}

		@Override
		public Metrics toDay() {
			return null;
		}

		@Override
		public int remoteHashCode() {
			return 0;
		}
	}

	@Test
	public void parse() throws IOException, ModuleStartException {
//		PrometheusHttpAPIRespBody body = readJsonFile("histogram.json");
		PrometheusHttpAPIRespBody body = readJsonFile("summary.json");
//		PrometheusHttpAPIRespBody body = readJsonFile("doubleAvg.json");
		JSONParser jsonParser = new JSONParser(body);
		MetricFamily mf = jsonParser.parse();
		
		Map<String, List<Metric>> idGroup = mf.getMetrics().stream().collect(Collectors.groupingBy(metrics->{
			String id = metrics.getLabels().get("id");//FIXME 会不会有漏洞
			return id;
		}));
		PrometheusMeterMapperFacade mapper = buildPrometheusMeterMapperFacade();
		Model model = new Model(NetworkAddressAlias.INDEX_NAME, null, null, DefaultScopeDefine.NETWORK_ADDRESS_ALIAS, DownSampling.Minute, false, false, PercentileMetricsMock.class);
		List<Metrics> result = new ArrayList<Metrics>();
		for(Entry<String, List<Metric>> entry : idGroup.entrySet()) {
			PercentileMetricsMock metrics = (PercentileMetricsMock) mapper.prometheusToSkywalking(model, entry.getValue());
			result.add(metrics);
		}
		System.out.println(result);
	}
	
	public static PrometheusHttpAPIRespBody readJsonFile(String file) {
		InputStream is = JSONParserTest.class.getResourceAsStream(file);
		Gson gson = new Gson();
		return gson.fromJson(new InputStreamReader(is, Charset.forName("gbk")), PrometheusHttpAPIRespBody.class);
	}
	
	
	private PrometheusMeterMapperFacade buildPrometheusMeterMapperFacade() throws ModuleStartException {
		PrometheusMeterMapperFacade.PrometheusMeterMapperFacadeBuilder builder = PrometheusMeterMapperFacade.builder();
		AnnotationScan scopeScan = new AnnotationScan();
		scopeScan.registerListener(new AnnotationListener() {
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void notify(Class aClass) throws StorageException {
				try {
					PrometheusMeterMapper mapper = (PrometheusMeterMapper) aClass.getDeclaredConstructor().newInstance();
					PrometheusMetricsMapper declaration = (PrometheusMetricsMapper) aClass.getAnnotation(PrometheusMetricsMapper.class);
					builder.delegate(declaration.value(), mapper);
				} catch (Exception e) {
					throw new StorageException(e.getMessage(), e);
				}
			}
			
			@Override
			public Class<? extends Annotation> annotation() {
				return PrometheusMetricsMapper.class;
			}
		});
		try {
			scopeScan.scan();
		} catch (IOException | StorageException e) {
			throw new ModuleStartException("load PrometheusMeterMapper failed!");
		}
		
		PrometheusMeterMapperFacade facade = builder.build();
		return facade;
	}
	
	public static void main(String[] args) {
		System.out.println(IDManager.ServiceID.analysisId("YWRtaW4tcHJvdmlkZXI=.1").getName());
	}
	
}
