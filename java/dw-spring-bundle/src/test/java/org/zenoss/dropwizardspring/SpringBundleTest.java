
package org.zenoss.dropwizardspring;

import com.google.common.eventbus.AsyncEventBus;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.tasks.Task;
import com.yammer.metrics.core.HealthCheck;
import org.junit.Assert;
import org.junit.Test;
import org.zenoss.dropwizardspring.eventbus.EventBusConfiguration;
import org.zenoss.dropwizardspring.testclasses.TestEventBus;
import org.zenoss.dropwizardspring.websockets.WebSocketConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class SpringBundleTest {

    class TestConfiguration extends Configuration implements SpringConfiguration {
        public WebSocketConfiguration getWebSocketConfiguration() {
            return new WebSocketConfiguration();
        }

        public EventBusConfiguration getEventBusConfiguration() {
            return new EventBusConfiguration();
        }
    }

    @Test
    public void scanTest() throws Exception {
        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
        Configuration config = new TestConfiguration();
        Environment environment = mock(Environment.class);
        Bootstrap bootStrap = mock(Bootstrap.class);
        ExecutorService service = mock(ExecutorService.class);
        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
        sb.initialize(bootStrap);
        sb.run(config, environment);
        verify(environment, times(1)).addResource(sb.applicationContext.getBean("fakeResource"));
        verify(environment, times(1)).addHealthCheck(sb.applicationContext.getBean("fakeHealthCheck", HealthCheck.class));
        verify(environment, times(1)).addTask(sb.applicationContext.getBean("fakeTask", Task.class));
        verify(environment, times(1)).manage(sb.applicationContext.getBean("fakeManaged", Managed.class));
    }

    @Test
    public void testSetDevProfile() throws Exception {
        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
        sb.setDefaultProfiles("dev");
        Configuration config = new TestConfiguration();
        Environment environment = mock(Environment.class);
        ExecutorService service = mock(ExecutorService.class);
        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
        sb.run(config, environment);
        Assert.assertTrue(sb.applicationContext.containsBean("devProfile"));
        Assert.assertFalse(sb.applicationContext.containsBean("testProfile"));
    }


    @Test
    public void testSetTestProfiles() throws Exception {
        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
        sb.setDefaultProfiles("dev");
        Configuration config = new TestConfiguration();
        Environment environment = mock(Environment.class);
        ExecutorService service = mock(ExecutorService.class);
        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
        sb.run(config, environment);
        Assert.assertTrue(sb.applicationContext.containsBean("devProfile"));
        Assert.assertFalse(sb.applicationContext.containsBean("testProfile"));
    }

    @Test
    public void testSetTwoProfiles() throws Exception {
        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
        sb.setDefaultProfiles("dev", "test");
        Configuration config = new TestConfiguration();
        Environment environment = mock(Environment.class);
        ExecutorService service = mock(ExecutorService.class);
        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
        sb.run(config, environment);
        Assert.assertTrue(sb.applicationContext.containsBean("devProfile"));
        Assert.assertTrue(sb.applicationContext.containsBean("testProfile"));
    }

    @Test
    public void testSetNoProfiles() throws Exception {
        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
        sb.setDefaultProfiles(new String[]{});
        Configuration config = new TestConfiguration();
        Environment environment = mock(Environment.class);
        ExecutorService service = mock(ExecutorService.class);
        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
        sb.run(config, environment);
        Assert.assertFalse(sb.applicationContext.containsBean("devProfile"));
        Assert.assertFalse(sb.applicationContext.containsBean("testProfile"));
    }

    @Test(expected = IllegalStateException.class)
    public void testNoPathWebSocket() throws Exception {
        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
        sb.setDefaultProfiles("broken");
        Configuration config = new TestConfiguration();
        Environment environment = mock(Environment.class);
        ExecutorService service = mock(ExecutorService.class);
        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
        sb.run(config, environment);
    }

    @Test
    public void testEventBus() throws Exception {
        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.testclasses");
        sb.setDefaultProfiles();
        Configuration config = new TestConfiguration();
        Environment environment = mock(Environment.class);
        ExecutorService service = mock(ExecutorService.class);
        when(environment.managedExecutorService(anyString(), anyInt(), anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(service);
        sb.run(config, environment);

        TestEventBus testEventBus = sb.applicationContext.getBean(TestEventBus.class);
        assertNotNull(testEventBus);
        assertNotNull(testEventBus.getSyncEventBus());
        assertNotNull(testEventBus.getAsynEventBus());
        assertThat(testEventBus.getSyncEventBus(), not(instanceOf(AsyncEventBus.class)));
        assertThat(testEventBus.getAsynEventBus(), instanceOf(AsyncEventBus.class));
    }
}
