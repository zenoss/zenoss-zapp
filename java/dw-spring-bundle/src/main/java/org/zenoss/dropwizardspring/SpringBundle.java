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

import com.google.common.base.Strings;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.ServletBuilder;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.tasks.Task;
import com.yammer.metrics.core.HealthCheck;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.zenoss.dropwizardspring.annotations.Resource;
import org.zenoss.dropwizardspring.eventbus.EventBusConfiguration;
import org.zenoss.dropwizardspring.websockets.SpringWebSocketServlet;
import org.zenoss.dropwizardspring.websockets.WebSocketConfiguration;

import javax.ws.rs.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Dropwizard bundle for configuring Spring using auto wired dependency injection by scanning for Spring components in
 * the configured packages.
 * <p/>
 * Automatically adds Dropwizard HealthChecks, Managed and Task objects that have annotations found in
 * {@link org.zenoss.dropwizardspring.annotations}. Also adds Jersey resources that have been annotated with
 * {@link org.zenoss.dropwizardspring.annotations.Resource}.
 * <p/>
 * Instantiate this class and register as a bundle in the initialize method in your Dropwizard main class.
 */

public final class SpringBundle implements ConfiguredBundle<Configuration> {

    AnnotationConfigApplicationContext applicationContext;
    private final String[] basePackages;
    private String[] profiles = new String[]{"prod"};
    private EventBus syncEventBus;
    private EventBus asyncEventBus;

    /**
     * Creates the SpringBundle that will scan the packages
     *
     * @param packages java packages that will be scanned for Spring components
     */
    public SpringBundle(String... packages) {
        this.basePackages = packages;
    }

    public void setDefaultProfiles(String... profiles) {
        this.profiles = profiles;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        initializeSpring(configuration, environment);

        // Do the dropwizard registrations
        addResources(environment);
        addHealthChecks(environment);
        addTasks(environment);
        addManaged(environment);
        addWebSockets(environment, ((SpringConfiguration) configuration).getWebSocketConfiguration());
    }

    private void initializeSpring(Configuration configuration, Environment environment) {
        if (applicationContext == null) {
            applicationContext = new AnnotationConfigApplicationContext();
            ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

            // Register the dropwizard config as a bean
            beanFactory.registerSingleton("dropwizard", configuration);
            beanFactory.registerSingleton("dropwizardEnvironment", environment);

            initializeEventBus(((SpringConfiguration) configuration).getEventBusConfiguration(), beanFactory, environment);

            //Set the default profile
            if (this.profiles != null && this.profiles.length > 0) {
                applicationContext.getEnvironment().setDefaultProfiles(this.profiles);
            }

            // Look for annotated things
            applicationContext.scan(basePackages);
            applicationContext.refresh();
        }
    }

    private void initializeEventBus(EventBusConfiguration config, ConfigurableListableBeanFactory beanFactory, Environment environment) {
        syncEventBus = new EventBus();
        beanFactory.registerSingleton("zapp::event-bus::sync", syncEventBus);

        int minThreads = config.getMinEventBusThreads();
        int maxThreads = config.getMaxEventBusThreads();
        int keepAliveMillis = config.getEventBusThreadKeepAliveMillis();
        ExecutorService executorService = environment.managedExecutorService("EventBusExecutorService", minThreads, maxThreads, keepAliveMillis, TimeUnit.MILLISECONDS);
        asyncEventBus = new AsyncEventBus(executorService);
        beanFactory.registerSingleton("zapp::event-bus::async", asyncEventBus);
    }


    private void addResources(Environment environment) {
        final Map<String, Object> resources = applicationContext.getBeansWithAnnotation(Resource.class);
        for (final Object resource : resources.values()) {
            environment.addResource(resource);
        }
    }

    private void addHealthChecks(Environment environment) {
        final Map<String, HealthCheck> healthChecks = applicationContext.getBeansOfType(HealthCheck.class);
        for (final HealthCheck healthCheck : healthChecks.values()) {
            environment.addHealthCheck(healthCheck);
        }
    }

    private void addTasks(Environment environment) {
        final Map<String, Task> tasks = applicationContext.getBeansOfType(Task.class);
        for (final Task task : tasks.values()) {
            environment.addTask(task);
        }
    }

    private void addManaged(Environment environment) {
        final Map<String, Managed> manageds = applicationContext.getBeansOfType(Managed.class);
        for (final Managed managed : manageds.values()) {
            environment.manage(managed);
        }
    }

    private void addWebSockets(Environment environment, WebSocketConfiguration config) {
        final Map<String, Object> listeners = applicationContext.getBeansWithAnnotation(org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener.class);
        int minThreads = config.getMinBroadcastThreads();
        int maxThreads = config.getMaxBroadcastThreads();
        int keepAliveMillis = config.getBroadcastThreadKeepAliveMillis();
        ExecutorService executorService = environment.managedExecutorService("WebSocketBroadcastExecutorService", minThreads, maxThreads, keepAliveMillis, TimeUnit.MILLISECONDS);
        for (final Object listener : listeners.values()) {
            Path path = listener.getClass().getAnnotation(Path.class);
            if (path == null || Strings.isNullOrEmpty(path.value())) {
                throw new IllegalStateException("Path must be defined: " + listener.getClass());
            }

            SpringWebSocketServlet wss = new SpringWebSocketServlet(listener, executorService, syncEventBus, asyncEventBus, path.value());
            ServletBuilder servletConfig = environment.addServlet(wss, path.value());

            if (config.getMaxTextMessageSize() != null) {
                servletConfig.setInitParam("maxTextMessageSize", String.valueOf(config.getMaxTextMessageSize()));
            }
            if (config.getMaxBinaryMessageSize() != null) {
                servletConfig.setInitParam("maxBinaryMessageSize", String.valueOf(config.getMaxBinaryMessageSize()));
            }
            if (config.getBufferSize() != null) {
                servletConfig.setInitParam("bufferSize", String.valueOf(config.getBufferSize()));
            }
            if (config.getMinVersion() != null) {
                servletConfig.setInitParam("minVersion", String.valueOf(config.getMinVersion()));
            }
            if (config.getMaxIdleTime() != null) {
                servletConfig.setInitParam("maxIdleTime", String.valueOf(config.getMaxIdleTime()));
            }
        }
    }
}
