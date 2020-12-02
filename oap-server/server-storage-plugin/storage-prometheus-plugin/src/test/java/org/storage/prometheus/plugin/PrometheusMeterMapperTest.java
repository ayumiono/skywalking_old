package org.storage.prometheus.plugin;

import java.io.IOException;
import java.lang.annotation.Annotation;

import org.apache.skywalking.oap.server.core.annotation.AnnotationListener;
import org.apache.skywalking.oap.server.core.annotation.AnnotationScan;
import org.apache.skywalking.oap.server.core.storage.StorageException;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapperFacade;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMetricsMapper;
import org.junit.Test;

public class PrometheusMeterMapperTest {

	@Test
	public void build() throws ModuleStartException {
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
		System.out.println(facade.getDelegates().size());
	}
	
}
