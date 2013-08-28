package org.zenoss.app.zauthbundle;

import com.google.common.base.Optional;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import io.ifar.dropwizard.shiro.ShiroBundle;
import io.ifar.dropwizard.shiro.ShiroConfiguration;
import org.zenoss.app.AppConfiguration;
import org.zenoss.app.annotations.Bundle;
import org.zenoss.app.autobundle.AutoConfiguredBundle;


@Bundle
public class ZAuthBundle implements AutoConfiguredBundle {


    public final ShiroBundle shiroBundle =
            new ShiroBundle<AppConfiguration>() {
                @Override
                public Optional<ShiroConfiguration> getShiroConfiguration(final AppConfiguration configuration) {
                    return Optional.<ShiroConfiguration>fromNullable(configuration.getShiroConfiguration());
                }
            };

    @Override
    public ConfiguredBundle getBundle(Bootstrap bootstrap) {
        return shiroBundle;
    }

    @Override
    public Optional<Class> getRequiredConfig() {
        return Optional.<Class>absent();
    }
}
