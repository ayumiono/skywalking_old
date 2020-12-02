package org.apache.skywalking.oap.server.storage.plugin.mixed;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.function.Function;

import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.storage.IBatchDAO;
import org.apache.skywalking.oap.server.core.storage.IHistoryDeleteDAO;
import org.apache.skywalking.oap.server.core.storage.StorageDAO;
import org.apache.skywalking.oap.server.core.storage.StorageModule;
import org.apache.skywalking.oap.server.core.storage.cache.INetworkAddressAliasDAO;
import org.apache.skywalking.oap.server.core.storage.management.UITemplateManagementDAO;
import org.apache.skywalking.oap.server.core.storage.profile.IProfileTaskLogQueryDAO;
import org.apache.skywalking.oap.server.core.storage.profile.IProfileTaskQueryDAO;
import org.apache.skywalking.oap.server.core.storage.profile.IProfileThreadSnapshotQueryDAO;
import org.apache.skywalking.oap.server.core.storage.query.IAggregationQueryDAO;
import org.apache.skywalking.oap.server.core.storage.query.IAlarmQueryDAO;
import org.apache.skywalking.oap.server.core.storage.query.ILogQueryDAO;
import org.apache.skywalking.oap.server.core.storage.query.IMetadataQueryDAO;
import org.apache.skywalking.oap.server.core.storage.query.IMetricsQueryDAO;
import org.apache.skywalking.oap.server.core.storage.query.ITopNRecordsQueryDAO;
import org.apache.skywalking.oap.server.core.storage.query.ITopologyQueryDAO;
import org.apache.skywalking.oap.server.core.storage.query.ITraceQueryDAO;
import org.apache.skywalking.oap.server.library.module.ApplicationConfiguration;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleConfigException;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.module.Service;
import org.apache.skywalking.oap.server.library.module.ServiceNotProvidedException;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.IAggregationQueryDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.IAlarmQueryDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.IBatchDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.IHistoryDeleteDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.ILogQueryDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.IMetadataQueryDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.IMetricsQueryDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.INetworkAddressAliasDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.IProfileTaskLogQueryDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.IProfileTaskQueryDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.IProfileThreadSnapshotQueryDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.ITopNRecordsQueryDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.ITopologyQueryDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.ITraceQueryDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.StorageDAOMixedImpl;
import org.apache.skywalking.oap.server.storage.plugin.mixed.impl.UITemplateManagementDAOMixedImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StorageModuleMixedProvider extends ModuleProvider {

	private static final Logger logger = LoggerFactory.getLogger(StorageModuleMixedProvider.class);

	protected final StorageModuleMixedConfig config;

	private ModuleProvider loadedProvider = null;
	
	private List<ModuleProvider> dependenciesProvider = new ArrayList<>();
	
	private Map<Class<?>/*service*/,Map<String/*provider name*/,Object/*service impl*/>> cache = new HashMap<>();

	public StorageModuleMixedProvider() {
		super();
		this.config = new StorageModuleMixedConfig();
	}
	
	@Override
	public String name() {
		return "mixed";
	}

	@Override
	public Class<? extends ModuleDefine> module() {
		return StorageModule.class;
	}

	@Override
	public ModuleConfig createConfigBeanIfAbsent() {
		return config;
	}

	@Override
	public void prepare() throws ServiceNotProvidedException, ModuleStartException {
        ServiceLoader<ModuleProvider> moduleProviderLoader = ServiceLoader.load(ModuleProvider.class);
		prepare(this.getManager(), config.getModuleConfigurationHolder(), moduleProviderLoader);
	}

	@Override
	public void start() throws ServiceNotProvidedException, ModuleStartException {
		log.debug("{} sub dependencies provider prepared for mixed storage provider", dependenciesProvider.size());
		for (ModuleProvider dependency : dependenciesProvider) {
			log.debug("dependency {} start", dependency.name());
			dependency.start();
		}
	}

	@Override
	public void notifyAfterCompleted() throws ServiceNotProvidedException, ModuleStartException {
	}

	@Override
	public String[] requiredModules() {
		return new String[] { CoreModule.NAME };
	}
	
	/**
	 * 检查是不是mixed需要的组件
	 * @param provider
	 * @return
	 */
	private boolean dependencies(String provider) {
		return config.getManagement().equals(provider) || 
				config.getMetrics().equals(provider) ||
				config.getNone().equals(provider) || 
				config.getRecord().equals(provider);
	}

	void prepare(ModuleManager moduleManager, ApplicationConfiguration.ModuleConfiguration configuration,
			ServiceLoader<ModuleProvider> moduleProviderLoader) {
		
		for (ModuleProvider provider : moduleProviderLoader) {
			try {
				if (!configuration.has(provider.name()) || !dependencies(provider.name()) || provider.name().equals("mixed")) {
					continue;
				}

				if (provider.module() == StorageModule.class) {
					loadedProvider = provider;
					loadedProvider.setManager(moduleManager);
					loadedProvider.setModuleDefine(this.getModule());
					
					logger.info("Prepare the {} provider in {} module.", loadedProvider.name(), this.name());
					try {
						copyProperties(loadedProvider.createConfigBeanIfAbsent(),
								configuration.getProviderConfiguration(loadedProvider.name()), this.name(), loadedProvider.name());
					} catch (IllegalAccessException e) {
						throw new ModuleConfigException(this.name() + " module config transport to config bean failure.", e);
					}
					loadedProvider.prepare();
					
					captureService(StorageDAO.class, loadedProvider);
					captureService(IBatchDAO.class, loadedProvider);
					captureService(INetworkAddressAliasDAO.class, loadedProvider);
					captureService(IMetadataQueryDAO.class, loadedProvider);
					captureService(ITopologyQueryDAO.class, loadedProvider);
					captureService(IMetricsQueryDAO.class, loadedProvider);
					captureService(ITraceQueryDAO.class, loadedProvider);
					captureService(IAggregationQueryDAO.class, loadedProvider);
					captureService(IAlarmQueryDAO.class, loadedProvider);
					captureService(ITopNRecordsQueryDAO.class, loadedProvider);
					captureService(ILogQueryDAO.class, loadedProvider);
					captureService(IProfileTaskQueryDAO.class, loadedProvider);
					captureService(IProfileThreadSnapshotQueryDAO.class, loadedProvider);
					captureService(IProfileTaskLogQueryDAO.class, loadedProvider);
					captureService(IHistoryDeleteDAO.class, loadedProvider);
					captureService(UITemplateManagementDAO.class, loadedProvider);
					dependenciesProvider.add(loadedProvider);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		
		//overwrite
		this.registerServiceImplementation(StorageDAO.class, new StorageDAOMixedImpl(getServiceCandidates(StorageDAO.class), config));
		this.registerServiceImplementation(IBatchDAO.class, new IBatchDAOMixedImpl(getServiceCandidates(IBatchDAO.class)));
		this.registerServiceImplementation(INetworkAddressAliasDAO.class, new INetworkAddressAliasDAOMixedImpl(config, getServiceCandidates(INetworkAddressAliasDAO.class)));
		this.registerServiceImplementation(IMetadataQueryDAO.class, new IMetadataQueryDAOMixedImpl(getServiceCandidates(IMetadataQueryDAO.class)));
		this.registerServiceImplementation(ITopologyQueryDAO.class, new ITopologyQueryDAOMixedImpl(config, getServiceCandidates(ITopologyQueryDAO.class)));
		this.registerServiceImplementation(IMetricsQueryDAO.class, new IMetricsQueryDAOMixedImpl(config, getServiceCandidates(IMetricsQueryDAO.class)));
		this.registerServiceImplementation(ITraceQueryDAO.class, new ITraceQueryDAOMixedImpl(config, getServiceCandidates(ITraceQueryDAO.class)));
		this.registerServiceImplementation(IAggregationQueryDAO.class, new IAggregationQueryDAOMixedImpl(config, getServiceCandidates(IAggregationQueryDAO.class)));
		this.registerServiceImplementation(IAlarmQueryDAO.class, new IAlarmQueryDAOMixedImpl(config, getServiceCandidates(IAlarmQueryDAO.class)));
		this.registerServiceImplementation(ITopNRecordsQueryDAO.class, new ITopNRecordsQueryDAOMixedImpl(config, getServiceCandidates(ITopNRecordsQueryDAO.class)));
		this.registerServiceImplementation(ILogQueryDAO.class, new ILogQueryDAOMixedImpl(config, getServiceCandidates(ILogQueryDAO.class)));//FIXME
		this.registerServiceImplementation(IProfileTaskQueryDAO.class, new IProfileTaskQueryDAOMixedImpl(config, getServiceCandidates(IProfileTaskQueryDAO.class)));
		this.registerServiceImplementation(IProfileThreadSnapshotQueryDAO.class, new IProfileThreadSnapshotQueryDAOMixedImpl(config, getServiceCandidates(IProfileThreadSnapshotQueryDAO.class)));
		this.registerServiceImplementation(IProfileTaskLogQueryDAO.class, new IProfileTaskLogQueryDAOMixedImpl(config, getServiceCandidates(IProfileTaskLogQueryDAO.class)));
		this.registerServiceImplementation(IHistoryDeleteDAO.class, new IHistoryDeleteDAOMixedImpl(getServiceCandidates(IHistoryDeleteDAO.class)));
		this.registerServiceImplementation(UITemplateManagementDAO.class, new UITemplateManagementDAOMixedImpl(config, getServiceCandidates(UITemplateManagementDAO.class)));
	}
	
	@SuppressWarnings("unchecked")
	private <T> Map<String, T> getServiceCandidates(Class<T> service) {
		if(!cache.containsKey(service)) {
			throw new ServiceNotProvidedException(service.getClass().getName() + " not provided");
		}
		Map<String, Object> candidates = cache.get(service);
		if(candidates.size() == 0) {
			throw new ServiceNotProvidedException(service.getClass().getName() + " not provided");
		}
		Map<String, T> rr = new HashMap<String, T>();
		for(Entry<String, Object> candidate : candidates.entrySet()) {
			rr.put(candidate.getKey(), (T)candidate.getValue());
		}
		return rr;
	}
	
	private <T extends Service> void captureService(Class<T> serviceType, ModuleProvider provider) {
		T serviceImpl = null;
		try {
			serviceImpl = provider.getService(serviceType);
		} catch (Exception e) {
			log.warn("{} provider not registry {} service implemention", provider.name(), serviceType.getName());
		}
		if(serviceImpl == null) return;
		cache.computeIfAbsent(serviceType, new Function<Class<?>, Map<String,Object>>() {
			@Override
			public Map<String, Object> apply(Class<?> t) {
				return new HashMap<String, Object>();
			}
		});
		Map<String, Object> c = cache.get(serviceType);
		c.put(loadedProvider.name(), serviceImpl);
	}

	private void copyProperties(ModuleConfig dest, Properties src, String moduleName, String providerName)
			throws IllegalAccessException {
		if (dest == null) {
			return;
		}
		Enumeration<?> propertyNames = src.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			Class<? extends ModuleConfig> destClass = dest.getClass();
			try {
				Field field = getDeclaredField(destClass, propertyName);
				field.setAccessible(true);
				field.set(dest, src.get(propertyName));
			} catch (NoSuchFieldException e) {
				logger.warn(propertyName + " setting is not supported in " + providerName + " provider of " + moduleName
						+ " module");
			}
		}
	}

	private Field getDeclaredField(Class<?> destClass, String fieldName) throws NoSuchFieldException {
		if (destClass != null) {
			Field[] fields = destClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.getName().equals(fieldName)) {
					return field;
				}
			}
			return getDeclaredField(destClass.getSuperclass(), fieldName);
		}

		throw new NoSuchFieldException();
	}

}
