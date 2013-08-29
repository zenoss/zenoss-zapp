package org.zenoss.app.zauthbundle;

import com.google.common.base.Optional;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.server.session.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenoss.app.AppConfiguration;
import org.zenoss.app.annotations.Bundle;
import org.zenoss.app.autobundle.AutoConfiguredBundle;
import org.zenoss.app.config.ProxyConfiguration;
import org.zenoss.app.config.ZappShiroConfiguration;

/**
 * Configured bundle that sets up shiro authentication for the zapp http requests.
 */
@Bundle
public class ZAuthBundle implements AutoConfiguredBundle {
    private static final Logger log = LoggerFactory.getLogger(ZAuthBundle.class);

    @Override
    public ConfiguredBundle getBundle(Bootstrap bootstrap) {
        return new ZAuthShiroBundle();
    }

    @Override
    public Optional<Class> getRequiredConfig() {
        return Optional.<Class>absent();
    }

    /**
     * This class initializes shiro in our environment.
     * @param <T> Configuration (AppConfiguration) for this bundle
     */
    static class ZAuthShiroBundle<T extends Configuration>
            implements ConfiguredBundle<T>{

        @Override
        public void run(T configuration, Environment environment) throws Exception {
            // get the proxy config so we can let the realm know where our host and port are.
            ProxyConfiguration proxyConfig = ((AppConfiguration) configuration).getProxyConfiguration();
            ZappShiroConfiguration config = ((AppConfiguration) configuration).getShiroConfiguration();

            // by default shiro is enabled but zapps can disabled it by specifying
            // shiro_configuration -> enabled: false in their configuration.yaml file
            if (config.isEnabled()) {
                if (config.isDropwizardSessionHandler() && environment.getSessionHandler() == null) {
                    environment.setSessionHandler(new SessionHandler());
                }
                // this allows individual zapps to specify a shiro.ini in their http section
                // i.e. http -> ContextParameter -> shiroConfigListeners
                // otherwise the default zauth bundle shir.ini is used.
                environment.addServletListeners(new EnvironmentLoaderListener());
                final String filterUrlPattern = config.getFilterUrlPattern();
                environment.addFilter(new ShiroFilter(), filterUrlPattern).setName("shiro-filter");
                TokenRealm.setProxyConfiguration(proxyConfig);
            }else {
                log.info("ZAuth security is disabled");
            }
        }

        @Override
        public void initialize(Bootstrap<?> bootstrap) {
            // no setup here, everything is done in run
        }
    }
}
