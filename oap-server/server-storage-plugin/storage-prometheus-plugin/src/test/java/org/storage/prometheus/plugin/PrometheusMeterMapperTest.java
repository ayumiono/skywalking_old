package org.storage.prometheus.plugin;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import org.apache.skywalking.oap.server.core.analysis.DownSampling;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.relation.service.ServiceRelationServerSideMetrics;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
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
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi;
import org.junit.Before;
import org.junit.Test;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
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
		new HTTPServer(8119);
	}
	
	@Test
	public void serviceRelationServerSideMetrics() throws IOException, InterruptedException {
		long timestamp = System.currentTimeMillis();
		System.out.println("timestamp:" + timestamp);
		Model model = new Model(ServiceRelationServerSideMetrics.INDEX_NAME, null, null, DefaultScopeDefine.SERVICE_RELATION, DownSampling.Minute, false, false, ServiceRelationServerSideMetrics.class);
		ServiceRelationServerSideMetrics metrics = new ServiceRelationServerSideMetrics();
		metrics.setEntityId("entity_id");
		metrics.setComponentId(1);
		metrics.setDestServiceId("dest_svc_id");
		metrics.setSourceServiceId("source_svc_id");
		metrics.setTimeBucket(TimeBucket.getTimeBucket(timestamp, DownSampling.Minute));
		
		System.out.println("id:" + metrics.id());
		for(int i=0;i<10;i++) {
			List<Metrics> cache = dao.multiGet(model, Collections.singletonList(metrics.id()));
			if(cache.size() > 0) {
				metrics.combine(cache.get(0));
			}
			InsertRequest mfs = dao.prepareBatchInsert(model, metrics);
			PrometheusInsertRequest _mfs = (PrometheusInsertRequest) mfs;
			new Collector() {
				@Override
				public List<MetricFamilySamples> collect() {
					return Collections.singletonList(_mfs.getMetricFamily());
				}
			}.register();
			Thread.sleep(20000L);
			CollectorRegistry.defaultRegistry.clear();
		}
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
