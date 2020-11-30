package org.apache.skywalking.oap.server.storage.plugin.prometheus;

import java.lang.annotation.Annotation;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.annotation.AnnotationListener;
import org.apache.skywalking.oap.server.core.annotation.AnnotationScan;
import org.apache.skywalking.oap.server.core.storage.IBatchDAO;
import org.apache.skywalking.oap.server.core.storage.StorageDAO;
import org.apache.skywalking.oap.server.core.storage.StorageException;
import org.apache.skywalking.oap.server.core.storage.StorageModule;
import org.apache.skywalking.oap.server.core.storage.query.IMetricsQueryDAO;
import org.apache.skywalking.oap.server.core.storage.query.ITopologyQueryDAO;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.module.ServiceNotProvidedException;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.base.BatchProcessPrometheusDAO;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.base.MetricsQueryPrometheusDAO;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.base.StoragePrometheusDao;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.base.TopologyQueryPrometheusDAO;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapperFacade;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMetricsMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi;

import io.prometheus.client.exporter.BasicAuthHttpConnectionFactory;
import io.prometheus.client.exporter.PushGateway;

public class StorageModulePrometheusProvider extends ModuleProvider {

	private final StorageModulePrometheusConfig config;
	
	public StorageModulePrometheusProvider() {
		super();
		this.config = new StorageModulePrometheusConfig(); 
	}
	
	@Override
	public void prepare() throws ServiceNotProvidedException, ModuleStartException {
		
		if(StringUtils.isBlank(config.getGatewayAddress())) {
			//TODO
		}
		
		PushGateway pushgateway = new PushGateway(config.getGatewayAddress());
		pushgateway.setConnectionFactory(new BasicAuthHttpConnectionFactory(config.getUser(), config.getPassword()));
		
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
					// TODO: handle exception
				}
			}
			
			@Override
			public Class<? extends Annotation> annotation() {
				return PrometheusMetricsMapper.class;
			}
		});
		
		//只需要注册以下几个服务实现
		//StorageDAO
		this.registerServiceImplementation(StorageDAO.class, new StoragePrometheusDao(config, builder.build()));//FIXME
		//IBatchDAO
		this.registerServiceImplementation(IBatchDAO.class, new BatchProcessPrometheusDAO(pushgateway));//TODO
		//IMetricsQueryDAO
		this.registerServiceImplementation(IMetricsQueryDAO.class, new MetricsQueryPrometheusDAO());//TODO
		//ITopologyQueryDAO
		this.registerServiceImplementation(ITopologyQueryDAO.class, new TopologyQueryPrometheusDAO(new PrometheusHttpApi(config.getPrometheusAddress())));
	}

	@Override
	public void start() throws ServiceNotProvidedException, ModuleStartException {
		// TODO 
		
	}

	@Override
	public void notifyAfterCompleted() throws ServiceNotProvidedException, ModuleStartException {
	}

	@Override
	public String[] requiredModules() {
		return new String[] {CoreModule.NAME};
	}

	@Override
	public String name() {
		return "prometheus";
	}

	@Override
	public Class<? extends ModuleDefine> module() {
		return StorageModule.class;
	}

	@Override
	public ModuleConfig createConfigBeanIfAbsent() {
		return config;
	}
}
