package org.zenoss.app.zauthbundle;

import com.google.common.base.Optional;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.server.session.SessionHandler;

import org.zenoss.app.AppConfiguration;
import org.zenoss.app.annotations.Bundle;
import org.zenoss.app.autobundle.AutoConfiguredBundle;
import org.zenoss.app.config.ProxyConfiguration;

/**
 * Configured bundle that sets up shiro authentication for the zapp http requests.
 */
@Bundle
public class ZAuthBundle implements AutoConfiguredBundle<AppConfiguration> {

    @Override
    public ConfiguredBundle getBundle(Bootstrap bootstrap) {
        return new ZAuthShiroBundle();
    }

    @Override
    public Optional<Class<AppConfiguration>> getRequiredConfig() {
        return Optional.of(AppConfiguration.class);
    }

    /**
     * This class initializes shiro in our environment.
     */
    static class ZAuthShiroBundle implements ConfiguredBundle<AppConfiguration> {

        private static final String URL_PATTERN = "/*";

        @Override
        public void run(AppConfiguration configuration, Environment environment) throws Exception {
            // get the proxy config so we can let the realm know where our host and port are.
            if (configuration.isAuthEnabled()) {
                ProxyConfiguration proxyConfig = configuration.getProxyConfiguration();

                if (environment.getSessionHandler() == null) {
                    environment.setSessionHandler(new SessionHandler());
                }

                // this allows individual zapps to specify a shiro.ini in their http section
                // i.e. http -> ContextParameter -> shiroConfigListeners
                // otherwise the default zauthbundle bundle shiro.ini is used.
                environment.addServletListeners(new EnvironmentLoaderListener());
                environment.addFilter(new ShiroFilter(), URL_PATTERN).setName("shiro-filter");
                TokenRealm.setProxyConfiguration(proxyConfig);
            }
        }

        @Override
        public void initialize(Bootstrap<?> bootstrap) {
            // no setup here, everything is done in run
        }
    }
}
