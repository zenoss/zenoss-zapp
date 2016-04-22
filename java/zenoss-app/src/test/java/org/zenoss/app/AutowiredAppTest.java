// Copyright 2014 The Serviced Authors.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package org.zenoss.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.setup.AdminEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.Assert;
import org.junit.Test;
import org.zenoss.app.autobundle.FakeAppConfig;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AutowiredAppTest {

    private static final class TestApp extends AutowiredApp<FakeAppConfig>{

        @Override
        public String getAppName() {
            return "Test App";
        }

        @Override
        protected Class<FakeAppConfig> getConfigType() {
            return FakeAppConfig.class;
        }

        @Override
        protected String[] getActivateProfiles() {
            return new String[]{"test"};
        }

    }

    @Test
    public void getTypeTest(){
        TestApp ta = new TestApp();
        Assert.assertEquals(FakeAppConfig.class, ta.getConfigType());
    }

    @Test
    public void testGetActivateProfiles(){
        TestApp ta = new TestApp();
        Assert.assertArrayEquals(new String[]{"test"}, ta.getActivateProfiles());

        AutowiredApp test = new AutowiredApp<AppConfiguration>(){
            @Override
            public String getAppName() {
                return "test";
            }

            @Override
            protected Class<AppConfiguration> getConfigType() {
                return AppConfiguration.class;
            }

        };

        Assert.assertArrayEquals(AutowiredApp.DEFAULT_ACTIVE_PROFILES, test.getActivateProfiles());


    }

    @Test
    public void testRun() throws Exception {
//        final Environment environment = mock(Environment.class);
//        final AdminEnvironment adminEnvironment = mock(AdminEnvironment.class);
//
//        final ObjectMapper om = mock(ObjectMapper.class);
//
//        when(environment.getObjectMapper()).thenReturn(om);
//        when(environment.admin()).thenReturn(adminEnvironment);
//
//        FakeAppConfig conf = new FakeAppConfig();
//        TestApp ta = new TestApp();
//        Bootstrap<FakeAppConfig> bootstrap = mock(Bootstrap.class);
//        ta.initialize(bootstrap);
//        ta.run(conf , environment);
//        verify(om).enable(SerializationFeature.INDENT_OUTPUT);

    }

    @Test
    public void testInitialize() throws Exception {
        final Environment environment = mock(Environment.class);
        Bootstrap bootstrap = mock(Bootstrap.class);

        ObjectMapper om = mock(ObjectMapper.class);
        when(environment.getObjectMapper()).thenReturn(om);
        TestApp ta = new TestApp();
        ta.initialize(bootstrap);

    }

}
