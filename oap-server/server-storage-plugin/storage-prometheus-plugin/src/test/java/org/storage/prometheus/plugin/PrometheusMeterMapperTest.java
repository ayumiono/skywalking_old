package org.storage.prometheus.plugin;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import org.apache.skywalking.oap.server.core.analysis.DownSampling;
import org.apache.skywalking.oap.server.core.analysis.IDManager;
import org.apache.skywalking.oap.server.core.analysis.NodeType;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.networkalias.NetworkAddressAlias;
import org.apache.skywalking.oap.server.core.annotation.AnnotationListener;
import org.apache.skywalking.oap.server.core.annotation.AnnotationScan;
import org.apache.skywalking.oap.server.core.source.DefaultScopeDefine;
import org.apache.skywalking.oap.server.core.storage.StorageException;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.base.MetricsPrometheusDAO;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.base.PrometheusInsertRequest;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapperFacade;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMetricsMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.CustomCollectorRegistry;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi;
import org.junit.Before;
import org.junit.Test;

import io.prometheus.client.Collector;
import io.prometheus.client.exporter.HTTPServer;

public class PrometheusMeterMapperTest {

	@Test
	public void build() throws ModuleStartException {
		PrometheusMeterMapperFacade facade = buildPrometheusMeterMapperFacade();
		System.out.println(facade.getDelegates().size());
	}
	
	MetricsPrometheusDAO dao;
	PrometheusMeterMapperFacade mapper;
	PrometheusHttpApi api;
	
	@Before
	public void init() throws IOException, ModuleStartException {
		mapper = buildPrometheusMeterMapperFacade();
		api = new PrometheusHttpApi("http://192.168.201.31:9090");
		dao = new MetricsPrometheusDAO(api, mapper);
		new HTTPServer(new InetSocketAddress(8119), CustomCollectorRegistry.defaultRegistry);
	}
	
	@Test
	public void networkAddress() throws IOException {
		
		Model model = new Model(NetworkAddressAlias.INDEX_NAME, null, null, DefaultScopeDefine.NETWORK_ADDRESS_ALIAS, DownSampling.Minute, false, false, NetworkAddressAlias.class);
		NetworkAddressAlias metrics = new NetworkAddressAlias();
		metrics.setAddress("localhost:20220");
		metrics.setLastUpdateTimeBucket(TimeBucket.getTimeBucket(System.currentTimeMillis(), DownSampling.Minute));
		metrics.setRepresentServiceId(IDManager.ServiceID.buildId("dubbo_mysteel_es_search_provider", NodeType.RPCFramework));
		metrics.setRepresentServiceInstanceId(IDManager.ServiceInstanceID.buildId(metrics.getRepresentServiceId(), metrics.getAddress()));
		metrics.setTimeBucket(metrics.getLastUpdateTimeBucket());
		InsertRequest mfs = dao.prepareBatchInsert(model, metrics);
		
		PrometheusInsertRequest _mfs = (PrometheusInsertRequest) mfs;
		
		new Collector() {
			@Override
			public List<MetricFamilySamples> collect() {
				return Collections.singletonList(_mfs.getMetricFamily());
			}
		}.register(CustomCollectorRegistry.defaultRegistry);
		
		LockSupport.park();
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
}
