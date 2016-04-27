// Copyright 2014, 2016 The Serviced Authors.
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
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.zenoss.dropwizardspring.annotations.Resource;
import org.zenoss.dropwizardspring.eventbus.EventBusConfiguration;
import org.zenoss.dropwizardspring.websockets.WebSocketConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;


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

public final class SpringBundle implements ConfiguredBundle<SpringConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(SpringBundle.class);


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

    public AnnotationConfigApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setDefaultProfiles(String... profiles) {
        this.profiles = profiles;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(final SpringConfiguration configuration, final Environment environment) throws Exception {
        log.info("_____spring scanning________*******************");
        initializeSpring(configuration, environment);
        // Do the dropwizard registrations
        addResources(environment);
        addHealthChecks(environment);
        addTasks(environment);
        addManaged(environment);
    }

    private void initializeSpring(SpringConfiguration configuration, Environment environment) {
        if (applicationContext == null) {
            applicationContext = new AnnotationConfigApplicationContext();
            ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

            // Register the dropwizard config as a bean
            beanFactory.registerSingleton("dropwizard", configuration);
            beanFactory.registerSingleton("dropwizardEnvironment", environment);

            initializeEventBus(configuration.getEventBusConfiguration(), beanFactory, environment);

            //Set the default profile
            if (this.profiles != null && this.profiles.length > 0) {
                applicationContext.getEnvironment().setDefaultProfiles(this.profiles);
            }

            // Look for annotated things
            List<String> scanPackages = new ArrayList<>(Arrays.asList(basePackages));
            scanPackages.add("org.zenoss.dropwizardspring");
            applicationContext.scan(scanPackages.toArray(new String[]{}));
            applicationContext.refresh();
        }
    }

    private void initializeEventBus(EventBusConfiguration config, ConfigurableListableBeanFactory beanFactory, Environment environment) {
        syncEventBus = new EventBus();
        beanFactory.registerSingleton("zapp::event-bus::sync", syncEventBus);

        int minThreads = config.getMinEventBusThreads();
        int maxThreads = config.getMaxEventBusThreads();
        int keepAliveMillis = config.getEventBusThreadKeepAliveMillis();
        ExecutorService executorService = environment.lifecycle().executorService("EventBusExecutorService")
                .minThreads(minThreads)
                .maxThreads(maxThreads)
                .keepAliveTime(Duration.milliseconds(keepAliveMillis))
                .build();
        asyncEventBus = new AsyncEventBus(executorService);
        beanFactory.registerSingleton("zapp::event-bus::async", asyncEventBus);
    }


    private void addResources(Environment environment) {
        final Map<String, Object> resources = applicationContext.getBeansWithAnnotation(Resource.class);
        for (final Object resource : resources.values()) {
            environment.jersey().register(resource);
        }
    }

    private void addHealthChecks(Environment environment) {
        final Map<String, HealthCheck> healthChecks = applicationContext.getBeansOfType(HealthCheck.class);
        for (final HealthCheck healthCheck : healthChecks.values()) {
            environment.healthChecks().register(healthCheck.getClass().toString(), healthCheck);
        }
    }

    private void addTasks(Environment environment) {
        final Map<String, Task> tasks = applicationContext.getBeansOfType(Task.class);
        for (final Task task : tasks.values()) {
            environment.admin().addTask(task);
        }
    }

    private void addManaged(Environment environment) {
        final Map<String, Managed> manageds = applicationContext.getBeansOfType(Managed.class);
        for (final Managed managed : manageds.values()) {
            environment.lifecycle().manage(managed);
        }
    }
}
