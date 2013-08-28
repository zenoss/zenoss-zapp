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

    static class ZAuthShiroBundle<T extends Configuration>
            implements ConfiguredBundle<T>{

        @Override
        public void run(T configuration, Environment environment) throws Exception {
            ProxyConfiguration proxyConfig = ((AppConfiguration) configuration).getProxyConfiguration();
            ZappShiroConfiguration config = ((AppConfiguration) configuration).getShiroConfiguration();

            if (config.isEnabled()) {
                if (config.isDropwizardSessionHandler() && environment.getSessionHandler() == null) {
                    environment.setSessionHandler(new SessionHandler());
                }
                environment.addServletListeners(new EnvironmentLoaderListener());
                final String filterUrlPattern = config.getFilterUrlPattern();
                log.debug("ShiroFilter will check URLs matching '{}'.", filterUrlPattern);
                environment.addFilter(new ShiroFilter(), filterUrlPattern).setName("shiro-filter");
            }else {
                log.info("Shiro security is disabled");
            }
        }

        @Override
        public void initialize(Bootstrap<?> bootstrap) {

        }
    }
}
