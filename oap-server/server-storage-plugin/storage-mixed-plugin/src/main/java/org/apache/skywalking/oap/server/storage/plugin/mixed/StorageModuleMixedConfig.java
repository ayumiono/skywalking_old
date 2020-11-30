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

package org.apache.skywalking.oap.server.storage.plugin.mixed;

import org.apache.skywalking.oap.server.library.module.ApplicationConfiguration.ModuleConfiguration;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleConfigurationAware;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageModuleMixedConfig extends ModuleConfig implements ModuleConfigurationAware {
	
	private String metrics;
	private String record;
	private String management;
	private String none;
	
	private ModuleConfiguration moduleConfigurationHolder;

	@Override
	public void injectModuleConfiguration(ModuleConfiguration moduleConfigurationHolder) {
        this.moduleConfigurationHolder = moduleConfigurationHolder;
	}
}
