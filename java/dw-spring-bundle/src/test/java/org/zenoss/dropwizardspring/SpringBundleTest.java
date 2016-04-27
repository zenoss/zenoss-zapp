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


package org.zenoss.dropwizardspring;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.eventbus.AsyncEventBus;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.AdminEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zenoss.dropwizardspring.eventbus.EventBusConfiguration;
import org.zenoss.dropwizardspring.testclasses.TestEventBus;
import org.zenoss.dropwizardspring.websockets.WebSocketConfiguration;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class SpringBundleTest {

    public class TestConfiguration extends Configuration implements SpringConfiguration {
        public WebSocketConfiguration getWebSocketConfiguration() {
            return new WebSocketConfiguration();
        }

        public EventBusConfiguration getEventBusConfiguration() {
            return new EventBusConfiguration();
        }
    }

    SpringBundle sb;

    @Before
    public void setup() {
        sb = new SpringBundle();
    }

    @Test
    public void scanTest() throws Exception {
        SpringConfiguration config = new TestConfiguration();
        Environment environment = mock(Environment.class, RETURNS_MOCKS);
        Bootstrap bootStrap = mock(Bootstrap.class);
        LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class, RETURNS_MOCKS);
        AdminEnvironment adminEnvironment = mock(AdminEnvironment.class);
        JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
        HealthCheckRegistry healthCheckRegistry = mock(HealthCheckRegistry.class);

        when(environment.healthChecks()).thenReturn(healthCheckRegistry);
        when(environment.jersey()).thenReturn(jerseyEnvironment);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.admin()).thenReturn(adminEnvironment);
        sb.initialize(bootStrap);
        sb.run(config, environment);
        verify(jerseyEnvironment, times(1)).register(sb.applicationContext.getBean("fakeResource"));
        verify(adminEnvironment, times(1)).addTask(sb.applicationContext.getBean("fakeTask", Task.class));
        verify(lifecycleEnvironment, times(1)).manage(sb.applicationContext.getBean("fakeManaged", Managed.class));
        HealthCheck hc = sb.applicationContext.getBean("fakeHealthCheck", HealthCheck.class);
        verify(healthCheckRegistry, times(1)).register(hc.getClass().toString(), hc);

    }

    @Test
    public void testSetDevProfile() throws Exception {
        sb.setDefaultProfiles("dev");
        SpringConfiguration config = new TestConfiguration();
        Environment environment = mock(Environment.class, RETURNS_MOCKS);
        sb.run(config, environment);
        Assert.assertTrue(sb.applicationContext.containsBean("devProfile"));
        Assert.assertFalse(sb.applicationContext.containsBean("testProfile"));
    }


    @Test
    public void testSetTestProfiles() throws Exception {
        sb.setDefaultProfiles("dev");
        SpringConfiguration config = new TestConfiguration();
        Environment environment = mock(Environment.class, RETURNS_MOCKS);
        sb.run(config, environment);
        Assert.assertTrue(sb.applicationContext.containsBean("devProfile"));
        Assert.assertFalse(sb.applicationContext.containsBean("testProfile"));
    }

    @Test
    public void testSetTwoProfiles() throws Exception {
        sb.setDefaultProfiles("dev", "test");
        SpringConfiguration config = new TestConfiguration();
        Environment environment = mock(Environment.class, RETURNS_MOCKS);
        sb.run(config, environment);
        Assert.assertTrue(sb.applicationContext.containsBean("devProfile"));
        Assert.assertTrue(sb.applicationContext.containsBean("testProfile"));
    }

    @Test
    public void testSetNoProfiles() throws Exception {
        sb.setDefaultProfiles(new String[]{});
        SpringConfiguration config = new TestConfiguration();
        Environment environment = mock(Environment.class, RETURNS_MOCKS);
        sb.run(config, environment);
        Assert.assertFalse(sb.applicationContext.containsBean("devProfile"));
        Assert.assertFalse(sb.applicationContext.containsBean("testProfile"));
    }

    @Test
    public void testEventBus() throws Exception {
        sb.setDefaultProfiles();
        SpringConfiguration config = new TestConfiguration();
        Environment environment = mock(Environment.class, RETURNS_MOCKS);
        sb.run(config, environment);

        TestEventBus testEventBus = sb.applicationContext.getBean(TestEventBus.class);
        assertNotNull(testEventBus);
        assertNotNull(testEventBus.getSyncEventBus());
        assertNotNull(testEventBus.getAsynEventBus());
        assertThat(testEventBus.getSyncEventBus(), not(instanceOf(AsyncEventBus.class)));
        assertThat(testEventBus.getAsynEventBus(), instanceOf(AsyncEventBus.class));
    }
}
