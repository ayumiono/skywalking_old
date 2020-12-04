package org.apache.skywalking.oap.server.storage.plugin.prometheus;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.InetSocketAddress;

import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.annotation.AnnotationListener;
import org.apache.skywalking.oap.server.core.annotation.AnnotationScan;
import org.apache.skywalking.oap.server.core.storage.IBatchDAO;
import org.apache.skywalking.oap.server.core.storage.StorageDAO;
import org.apache.skywalking.oap.server.core.storage.StorageException;
import org.apache.skywalking.oap.server.core.storage.StorageModule;
import org.apache.skywalking.oap.server.core.storage.cache.INetworkAddressAliasDAO;
import org.apache.skywalking.oap.server.core.storage.query.IMetricsQueryDAO;
import org.apache.skywalking.oap.server.core.storage.query.ITopologyQueryDAO;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.module.ServiceNotProvidedException;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.base.BatchProcessPrometheusDAO;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.base.MetricsQueryPrometheusDAO;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.base.NetworkAddressAliasPrometheusDAO;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.base.StoragePrometheusDao;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.base.TopologyQueryPrometheusDAO;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMeterMapperFacade;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.mapper.PrometheusMetricsMapper;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.CustomCollectorRegistry;
import org.apache.skywalking.oap.server.storage.plugin.prometheus.util.PrometheusHttpApi;

import io.prometheus.client.exporter.HTTPServer;

public class StorageModulePrometheusProvider extends ModuleProvider {

	private final StorageModulePrometheusConfig config;
	
	private CustomCollectorRegistry defaultRegistry;
	
	public StorageModulePrometheusProvider() {
		super();
		this.config = new StorageModulePrometheusConfig(); 
	}
	
	@Override
	public void prepare() throws ServiceNotProvidedException, ModuleStartException {
		
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
		
		PrometheusHttpApi api = new PrometheusHttpApi(config.getPrometheusAddress());
		
		
		
		//只需要注册以下几个服务实现
		//StorageDAO
		this.registerServiceImplementation(StorageDAO.class, new StoragePrometheusDao(config, builder.build()));
		//IBatchDAO
		this.registerServiceImplementation(IBatchDAO.class, new BatchProcessPrometheusDAO());
		//IMetricsQueryDAO
		this.registerServiceImplementation(IMetricsQueryDAO.class, new MetricsQueryPrometheusDAO());//TODO
		//ITopologyQueryDAO
		this.registerServiceImplementation(ITopologyQueryDAO.class, new TopologyQueryPrometheusDAO(api));
		//INetworkAddressAliasDAO
		this.registerServiceImplementation(INetworkAddressAliasDAO.class, new NetworkAddressAliasPrometheusDAO(api));
	}

	@Override
	public void start() throws ServiceNotProvidedException, ModuleStartException {
		try {
			CustomCollectorRegistry.defaultRegistry.init(getManager());
			new HTTPServer(new InetSocketAddress(config.getPrometheusHTTPServerHost(), config.getPrometheusHTTPServerPort()), CustomCollectorRegistry.defaultRegistry);
		} catch (Exception e) {
			throw new ModuleStartException(e.getMessage(), e);
		}
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
