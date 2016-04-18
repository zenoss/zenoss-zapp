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
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.zenoss.app.ZenossCredentials.Builder;
import org.zenoss.app.autobundle.BundleLoader;
import org.zenoss.app.tasks.DebugToggleTask;
import org.zenoss.app.tasks.LoggerLevelTask;
import org.zenoss.dropwizardspring.SpringBundle;

/**
 * Creates an App that uses Spring to scan and autowire objects. By default will scan for the Spring components with
 * profiles "prod" and "runtime".  The runtime profile should be used for classes that only need to be active during the
 * running of the zapp i.e. not during tests.
 *
 * @param <T>
 */
public abstract class AutowiredApp<T extends AppConfiguration> extends Application<T> {

    public static final String DEFAULT_SCAN = "org.zenoss.app";
    public static final String[] DEFAULT_ACTIVE_PROFILES = new String[]{"prod", "runtime"};

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
        SpringBundle sb = new SpringBundle(getScanPackages());
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
     * @return Class of parametrized type
     */
    protected abstract Class<T> getConfigType();

    /**
     * {@inheritDoc}
     */
    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        environment.admin().addTask(new LoggerLevelTask());
        environment.admin().addTask(new DebugToggleTask());
        environment.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }
}
