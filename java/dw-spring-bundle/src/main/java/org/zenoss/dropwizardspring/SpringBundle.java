/*
 * ****************************************************************************
 *
 *  Copyright (C) Zenoss, Inc. 2013, all rights reserved.
 *
 *  This content is made available according to terms specified in
 *  License.zenoss under the directory where your Zenoss product is installed.
 *
 * ***************************************************************************
 */

package org.zenoss.dropwizardspring;

import com.google.common.base.Strings;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.tasks.Task;
import com.yammer.metrics.core.HealthCheck;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.zenoss.dropwizardspring.annotations.Resource;
import org.zenoss.dropwizardspring.websockets.SpringWebSocketServlet;

import javax.ws.rs.Path;
import java.util.Map;


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

    /**
     * Creates the SpringBundle that will scan the packages
     *
     * @param packages java packages that will be scanned for Spring components
     */
    public SpringBundle( String... packages) {
        this.basePackages = packages;
    }

    public void setDefaultProfiles(String... profiles){
        this.profiles = profiles;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {

        initializeSpring(configuration);

        // Do the dropwizard registrations
        addResources(environment);
        addHealthChecks(environment);
        addTasks(environment);
        addManaged(environment);
        addWebSockets(environment);

    }

    private void initializeSpring(Configuration configuration) {
        if (applicationContext == null) {
            applicationContext = new AnnotationConfigApplicationContext();
            ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

            // Register the dropwizard config as a bean
            beanFactory.registerSingleton("dropwizard", configuration);

            //Set the default profile
            applicationContext.getEnvironment().setDefaultProfiles(this.profiles);

            // Look for annotated things
            applicationContext.scan(basePackages);

            applicationContext.refresh();
        }
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

    private void addWebSockets(Environment environment) {
        final Map<String, Object> listeners = applicationContext.getBeansWithAnnotation(org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener.class);
        for (final Object listener : listeners.values()) {
            SpringWebSocketServlet wss = new SpringWebSocketServlet(listener);
            String path = listener.getClass().getAnnotation(Path.class).value();
            if (Strings.isNullOrEmpty(path)){
                throw new IllegalStateException("Path must be defined: "+listener.getClass());
            }
            environment.addServlet(wss, path);
        }



    }

}
