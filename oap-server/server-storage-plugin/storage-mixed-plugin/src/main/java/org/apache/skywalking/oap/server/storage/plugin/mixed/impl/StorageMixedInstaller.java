package org.apache.skywalking.oap.server.storage.plugin.mixed.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.skywalking.oap.server.core.analysis.Stream;
import org.apache.skywalking.oap.server.core.analysis.StreamDefinition;
import org.apache.skywalking.oap.server.core.storage.StorageException;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.core.storage.model.ModelInstaller;
import org.apache.skywalking.oap.server.library.client.Client;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.storage.plugin.mixed.StorageModuleMixedConfig;

public class StorageMixedInstaller extends ModelInstaller {
	
	private Map<Class<?>, ModelInstaller> modelInstallers = new HashMap<>();
	
	private final StorageModuleMixedConfig config;

	public StorageMixedInstaller(Client client, ModuleManager moduleManager, StorageModuleMixedConfig config) {
		super(client, moduleManager);
		this.config = config;
	}

	@Override
	public boolean isExists(Model model) throws StorageException {
		Stream stream = (Stream) model.getStorageModelClazz().getAnnotation(Stream.class);
		StreamDefinition streamDefinition = StreamDefinition.from(stream);
		try {
			Method isExistsMethod = ModelInstaller.class.getMethod("isExists", Model.class);
			isExistsMethod.setAccessible(true);
			return (boolean) isExistsMethod.invoke(modelInstallers.get(streamDefinition.getProcessor()), model);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new StorageException(e.getMessage(), e);
		}
	}

	@Override
	public void createTable(Model model) throws StorageException {
		Stream stream = (Stream) model.getStorageModelClazz().getAnnotation(Stream.class);
		StreamDefinition streamDefinition = StreamDefinition.from(stream);
		try {
			Method createTableMethod = ModelInstaller.class.getMethod("createTable", Model.class);
			createTableMethod.setAccessible(true);
			createTableMethod.invoke(modelInstallers.get(streamDefinition.getProcessor()), model);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new StorageException(e.getMessage(), e);
		}
	}
}
