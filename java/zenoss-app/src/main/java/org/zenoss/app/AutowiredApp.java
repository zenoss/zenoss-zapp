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

import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.Application;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.Set;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.ResourceFinder;
import org.glassfish.jersey.server.internal.scanning.AnnotationAcceptingListener;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.zenoss.app.ZenossCredentials.Builder;
import org.zenoss.app.autobundle.BundleLoader;
import org.zenoss.app.tasks.DebugToggleTask;
import org.zenoss.dropwizardspring.SpringBundle;

import javax.websocket.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;


/**
 * Creates an App that uses Spring to scan and autowire objects. By default will scan for the Spring components with
 * profiles "prod" and "runtime".  The runtime profile should be used for classes that only need to be active during the
 * running of the zapp i.e. not during tests.
 *
 * @param <T> The configuration class derived from {@link AppConfiguration} to be used for this web app.
 */
public abstract class AutowiredApp<T extends AppConfiguration> extends Application<T> {

    public static final String DEFAULT_SCAN = "org.zenoss.app";
    public static final String[] DEFAULT_ACTIVE_PROFILES = new String[]{"prod", "runtime"};
    private SpringBundle sb;
    private boolean loadSwagger = false;
    private boolean enableCors = false;

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
    protected String[] getScanPackages() {
        return new String[]{DEFAULT_SCAN};
    }

    /**
     * The Spring profile activated by default.
     *
     * @return String[] of profiles to activate.
     */
    protected String[] getActivateProfiles() {
        return DEFAULT_ACTIVE_PROFILES;
    }

    /**
     * Flag to determine if the swagger configuration bundle must be loaded.
     *
     * @return true if the swagger configuration bundle should be loaded.
     */
    public boolean isLoadSwagger() {
        return loadSwagger;
    }

    /**
     * Flag that specifies that CORS should be enabled.  When CORS is enabled several configuration
     * parameters are leveraged that specify the methods, origins and headers to use (refer to
     * {@link org.zenoss.app.config.CorsConfiguration}).
     *
     * CORS is disabled by default.
     *
     * @return true if CORS is enabled.
     */
    public boolean isEnableCors() {
        return enableCors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void initialize(Bootstrap<T> bootstrap) {
        ConfiguredBundle<AppConfiguration> cb = new ConfiguredBundle<AppConfiguration>() {
            @Override
            public void run(AppConfiguration configuration, Environment environment) throws Exception {
                ZenossCredentials creds = configuration.getZenossCredentials();
                if (creds == null || creds.getUsername() == null || creds.getUsername().isEmpty()) {
                    configuration.setZenossCredentials(new Builder().getFromGlobalConf());
                }
            }

            @Override
            public void initialize(Bootstrap<?> bootstrap) {

            }
        };
        bootstrap.addBundle(cb);

        if(isLoadSwagger()){
            bootstrap.addBundle(new SwaggerBundle<AppConfiguration>() {
                @Override
                protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(AppConfiguration configuration) {
                    return configuration.getSwaggerBundleConfiguration();
                }
            });
        }

        sb = new SpringBundle(getScanPackages());
        sb.setDefaultProfiles(this.getActivateProfiles());
        bootstrap.addBundle(sb);

        Class configType = getConfigType();
        try {
            new BundleLoader().loadBundles(bootstrap, configType, getScanPackages());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * return the generic type of this class.
     *
     * @return Class of parametrized type
     */
    protected abstract Class<T> getConfigType();

    /**
     * {@inheritDoc}
     */
    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        environment.admin().addTask(new DebugToggleTask());
        environment.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        final AnnotationConfigApplicationContext ctx = sb.getApplicationContext();

        //find classes with ServerEndPoint annotation
        Set<Class<?>> serverEndpoints = findWS(ServerEndpoint.class, getScanPackages());

        ServerFactoryWithEndpoints sf = new ServerFactoryWithEndpoints(
                configuration.getServerFactory(),
                configuration.getWebsocketConfiguration());
        configuration.setServerFactory(sf);

        //find spring beans with ServerEndpoint annotation
        String[] names = ctx.getBeanNamesForAnnotation(ServerEndpoint.class);
        for (final String name : names) {
            final Class<?> clazz = ctx.getType(name);
            //remove spring ServerEndpoint from set of all endpoints
            serverEndpoints.remove(clazz);
            ServerEndpoint se = clazz.getAnnotation(ServerEndpoint.class);
            ServerEndpointConfig endpointConfig = ServerEndpointConfig.Builder.
                    create(clazz, se.value()).
                    configurator(new Configurator() {
                        @Override
                        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
                            return ctx.getBean(name, endpointClass);
                        }
                    }).build();
            sf.addEndpoint(endpointConfig);
        }
        //register any remaining endpoints that were not springified
        for (Class ws : serverEndpoints) {
            sf.addEndpoint(ws);
        }

        if (isEnableCors()) {
            FilterRegistration.Dynamic corsFilter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
            corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, configuration.getCorsConfiguration().getMethods());
            corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, configuration.getCorsConfiguration().getOrigins());
            corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, configuration.getCorsConfiguration().getHeaders());
            corsFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, configuration.getCorsConfiguration().getUrlMapping());
        }
    }

    Set<Class<?>> findWS(final Class<? extends Annotation> klazz, String... packages) throws IOException {
        final AnnotationAcceptingListener aal = new AnnotationAcceptingListener(klazz);
        ResourceFinder rf = new PackageNamesScanner(packages, true);
        while (rf.hasNext()) {
            final String next = rf.next();
            if (aal.accept(next)) {
                final InputStream in = rf.open();
                aal.process(next, in);
                in.close();
            }
        }
        return aal.getAnnotatedClasses();
    }
}
