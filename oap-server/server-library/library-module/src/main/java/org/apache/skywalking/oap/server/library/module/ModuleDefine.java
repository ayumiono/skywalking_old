/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.library.module;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A module definition.
 */
public abstract class ModuleDefine implements ModuleProviderHolder {

    private static final Logger logger = LoggerFactory.getLogger(ModuleDefine.class);

    private ModuleProvider loadedProvider = null;

    private final String name;

    public ModuleDefine(String name) {
        this.name = name;
    }

    /**
     * @return the module name
     */
    public final String name() {
        return name;
    }

    /**
     * @return the {@link Service} provided by this module.
     */
    public abstract Class[] services();

    /**
     * Run the prepare stage for the module, including finding all potential providers, and asking them to prepare.
     *
     * @param moduleManager of this module
     * @param configuration of this module
     * @throws ProviderNotFoundException when even don't find a single one providers.
     * 
     * modify by xuelong.chen 2020-11-24 storage module add mixed type provider
     */
    void prepare(ModuleManager moduleManager, ApplicationConfiguration.ModuleConfiguration configuration,
        ServiceLoader<ModuleProvider> moduleProviderLoader) throws ProviderNotFoundException, ServiceNotProvidedException, ModuleConfigException, ModuleStartException {
        if (this.name.equals("storage") && configuration.has("mixed")) {
            Properties properties = configuration.getProviderConfiguration("mixed");
            String metrics = properties.getProperty("metrics");
            if (!configuration.has(metrics)) {
                throw new ModuleConfigException(this.name() + "mixed provider dependencies module [metrics] missed");
            }
            String management = properties.getProperty("management");
            if (!configuration.has(management)) {
                throw new ModuleConfigException(this.name() + "mixed provider dependencies module [management] missed");
            }
            String record = properties.getProperty("record");
            if (!configuration.has(record)) {
                throw new ModuleConfigException(this.name() + "mixed provider dependencies module [record] missed");
            }
            String none = properties.getProperty("none");
            if (!configuration.has(none)) {
                throw new ModuleConfigException(this.name() + "mixed provider dependencies module [none] missed");
            }
            for (ModuleProvider provider : moduleProviderLoader) {
                if (provider.module().equals(getClass()) && provider.name().equals("mixed")) { //storage module mixed provider
                    if (loadedProvider == null) {
                        loadedProvider = provider;
                        loadedProvider.setManager(moduleManager);
                        loadedProvider.setModuleDefine(this);
                    } else {
                        throw new DuplicateProviderException(this.name() + " module has one " + loadedProvider.name() + "[" + loadedProvider
                                .getClass()
                                .getName() + "] provider already, " + provider.name() + "[" + provider.getClass()
                                                                                                      .getName() + "] is defined as 2nd provider.");
                    }
                }
            }
        } else {
            for (ModuleProvider provider : moduleProviderLoader) {
                if (!configuration.has(provider.name())) {
                    continue;
                }

                if (provider.module().equals(getClass())) {
                    if (loadedProvider == null) {
                        loadedProvider = provider;
                        loadedProvider.setManager(moduleManager);
                        loadedProvider.setModuleDefine(this);
                    } else {
                        throw new DuplicateProviderException(this.name() + " module has one " + loadedProvider.name() + "[" + loadedProvider
                            .getClass()
                            .getName() + "] provider already, " + provider.name() + "[" + provider.getClass()
                                                                                                  .getName() + "] is defined as 2nd provider.");
                    }
                }
            }
        }

        if (loadedProvider == null) {
            throw new ProviderNotFoundException(this.name() + " module no provider exists.");
        }

        logger.info("Prepare the {} provider in {} module.", loadedProvider.name(), this.name());
        try {
            ModuleConfig config = loadedProvider.createConfigBeanIfAbsent();
            if (config != null) {
                if (ModuleConfigurationAware.class.isAssignableFrom(config.getClass())) {
                    ((ModuleConfigurationAware) config).injectModuleConfiguration(configuration);
                }
            }
            copyProperties(config, configuration.getProviderConfiguration(loadedProvider
                .name()), this.name(), loadedProvider.name());
        } catch (IllegalAccessException e) {
            throw new ModuleConfigException(this.name() + " module config transport to config bean failure.", e);
        }
        loadedProvider.prepare();
    }

    private void copyProperties(ModuleConfig dest, Properties src, String moduleName,
        String providerName) throws IllegalAccessException {
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
                logger.warn(propertyName + " setting is not supported in " + providerName + " provider of " + moduleName + " module");
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

    @Override
    public final ModuleProvider provider() throws DuplicateProviderException, ProviderNotFoundException {
        if (loadedProvider == null) {
            throw new ProviderNotFoundException("There is no module provider in " + this.name() + " module!");
        }

        return loadedProvider;
    }
}
