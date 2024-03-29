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

package org.apache.skywalking.oap.server.cluster.plugin.kubernetes;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.cluster.ClusterModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class ClusterModuleKubernetesProviderTest {

    private ClusterModuleKubernetesProvider provider = new ClusterModuleKubernetesProvider();

    @Test
    public void name() {
        assertEquals("kubernetes", provider.name());
    }

    @Test
    public void module() {
        assertEquals(ClusterModule.class, provider.module());
    }

    @Test
    public void createConfigBeanIfAbsent() {
    	ClusterModuleKubernetesConfig moduleConfig = (ClusterModuleKubernetesConfig) provider.createConfigBeanIfAbsent();
    	moduleConfig.setK8sApiServerBasePath("http://192.168.204.107:8080");
    	moduleConfig.setNamespace("test");
    	moduleConfig.setLabelSelector("app=skywalking,release=skywalking,component=oap");
    	moduleConfig.setUidEnvName("SKYWALKING_COLLECTOR_UID");
        NamespacedPodListInformer.INFORMER.init(moduleConfig);
        
        assertTrue(moduleConfig instanceof ClusterModuleKubernetesConfig);
    }

    @Test
    public void prepare() throws Exception {
        provider.prepare();
    }

    @Test
    public void notifyAfterCompleted() {
        provider.notifyAfterCompleted();
    }

    @Test
    public void requiredModules() {
        String[] modules = provider.requiredModules();
        assertArrayEquals(new String[] {CoreModule.NAME}, modules);
    }
}