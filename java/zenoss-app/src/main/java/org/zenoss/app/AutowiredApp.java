package org.zenoss.app;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.metrics.reporting.HealthCheckServlet;
import com.yammer.metrics.reporting.MetricsServlet;
import org.zenoss.dropwizardspring.bundle.SpringBundle;

import javax.servlet.Servlet;

/**
 * Creates an App that uses Spring to scan and autowire objects.
 *
 * @param <T>
 */
public abstract class AutowiredApp<T extends AppConfiguration> extends Service<T> {

    public static final String DEFAULT_SCAN = "org.zenoss.app";

    /**
     * The app name
     *
     * @return String the name of the App
     */
    public abstract String getAppName();

    /**
     * Java packages that will be scanned to load and autowire objects.
     *
     * @return String[] of packages to scan.
     */
    protected String[] getScanPackages(){
        return new String[]{DEFAULT_SCAN};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void initialize(Bootstrap<T> bootstrap) {
        bootstrap.setName(this.getAppName());
        bootstrap.addBundle(new SpringBundle(getScanPackages()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        Servlet metrics = new MetricsServlet();
        Servlet healthcheck = new HealthCheckServlet();
        environment.addServlet(metrics, "/metrics");
        environment.addServlet(healthcheck, "/healthcheck");
        environment.getObjectMapperFactory().enable(SerializationFeature.INDENT_OUTPUT);
    }
}
