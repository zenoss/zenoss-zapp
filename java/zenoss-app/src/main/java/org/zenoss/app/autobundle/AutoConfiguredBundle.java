
package org.zenoss.app.autobundle;

import com.google.common.base.Optional;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;

import org.zenoss.app.AppConfiguration;

/**
 *
 * Interface for configuring a drop wizard bundle that can be automatically loaded.
 *
 */
public interface AutoConfiguredBundle<T extends AppConfiguration> {

    /**
     * Get the initializedbundle to be loaded.
     *
     * @return ConfiguredBundle to be added
     */
    ConfiguredBundle getBundle(Bootstrap bootstrap);

    /**
     * Asserts that the class returned should be the same, a superclass or a super interface of the App Configuration
     *
     * @return Class Configuration type expected by the bundle
     */
    Optional<Class<T>> getRequiredConfig();

}
