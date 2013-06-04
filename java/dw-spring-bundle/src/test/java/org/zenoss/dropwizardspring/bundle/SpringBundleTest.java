package org.zenoss.dropwizardspring.bundle;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.tasks.Task;
import com.yammer.metrics.core.HealthCheck;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class SpringBundleTest {

    @Test
    public void scanTest() throws Exception {

        SpringBundle sb = new SpringBundle("org.zenoss.dropwizardspring.bundle.testclasses");
        Configuration config = mock(Configuration.class);
        Environment environment = mock(Environment.class);
        sb.run(config, environment);
        verify(environment,times(1)).addResource(sb.applicationContext.getBean("fakeResource"));
        verify(environment, times(1)).addHealthCheck(sb.applicationContext.getBean("fakeHealthCheck", HealthCheck.class));
        verify(environment, times(1)).addTask(sb.applicationContext.getBean("fakeTask", Task.class));
        verify(environment, times(1)).manage(sb.applicationContext.getBean("fakeManaged", Managed.class));
    }
}
