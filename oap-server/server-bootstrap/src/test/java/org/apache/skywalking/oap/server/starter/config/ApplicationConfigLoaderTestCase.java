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

package org.apache.skywalking.oap.server.starter.config;

import java.util.Properties;
import org.apache.skywalking.oap.server.library.module.ApplicationConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ApplicationConfigLoaderTestCase {

    private ApplicationConfiguration applicationConfiguration;

    @Before
    public void setUp() throws ConfigFileNotFoundException {
//        System.setProperty("SW_STORAGE", "mysql");
        ApplicationConfigLoader configLoader = new ApplicationConfigLoader();
        applicationConfiguration = configLoader.load();
    }

    @Test
    public void testLoadConfig() {
        Properties providerConfig = applicationConfiguration.getModuleConfiguration("storage")
                                                            .getProviderConfiguration("mysql");
        assertThat(providerConfig.get("metadataQueryMaxSize"), is(5000));
        assertThat(providerConfig.get("properties"), instanceOf(Properties.class));
        Properties properties = (Properties) providerConfig.get("properties");
        assertThat(properties.get("jdbcUrl"), is("jdbc:mysql://localhost:3306/swtest"));
    }
}
