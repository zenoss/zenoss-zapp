
package org.zenoss.app;

import org.zenoss.app.config.ProxyConfiguration;
import org.zenoss.dropwizardspring.SpringConfiguration;
import org.zenoss.dropwizardspring.eventbus.EventBusConfiguration;
import org.zenoss.dropwizardspring.websockets.WebSocketConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;


/**
 * Base class for App configurations. Implementations should add configuration properties and optionally
 * sub configuration objects.
 */

public abstract class AppConfiguration extends Configuration implements SpringConfiguration {
    @JsonProperty
    private ProxyConfiguration proxyConfiguration = new ProxyConfiguration();

    @JsonProperty
    private WebSocketConfiguration webSocketConfiguration = new WebSocketConfiguration();

    @JsonProperty
    private EventBusConfiguration eventBusConfiguration = new EventBusConfiguration();

    @JsonProperty
    private ZenossCredentials zenossCredentials;

    @JsonProperty
    private boolean authEnabled = true;

    public ProxyConfiguration getProxyConfiguration() {
        return proxyConfiguration;
    }

    public WebSocketConfiguration getWebSocketConfiguration() {
        return webSocketConfiguration;
    }

    public EventBusConfiguration getEventBusConfiguration() {
        return eventBusConfiguration;
    }

    public void setProxyConfiguration(ProxyConfiguration proxyConfiguration) {
        this.proxyConfiguration = proxyConfiguration;
    }

    public void setWebSocketConfiguration(WebSocketConfiguration webSocketConfiguration) {
        this.webSocketConfiguration = webSocketConfiguration;
    }

    public void setEventBusConfiguration(EventBusConfiguration eventBusConfiguration) {
        this.eventBusConfiguration = eventBusConfiguration;
    }

    public void setZenossCredentials(ZenossCredentials creds) {
        this.zenossCredentials = creds;
    }

    public ZenossCredentials getZenossCredentials() {
        return this.zenossCredentials;
    }

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public void setAuthEnabled(boolean authEnabled) {
        this.authEnabled = authEnabled;
    }
}
