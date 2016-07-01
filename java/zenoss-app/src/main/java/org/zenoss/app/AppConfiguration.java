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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import org.zenoss.app.config.ProxyConfiguration;
import org.zenoss.dropwizardspring.SpringConfiguration;
import org.zenoss.dropwizardspring.eventbus.EventBusConfiguration;
import org.zenoss.dropwizardspring.websockets.WebSocketConfiguration;


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

    @JsonProperty
    private int authTimeoutSeconds = 60;

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

    public int getAuthTimeoutSeconds() {
        return this.authTimeoutSeconds;
    }

    public void setAuthTimeoutSeconds(int seconds) {
        this.authTimeoutSeconds = seconds;
    }
}
