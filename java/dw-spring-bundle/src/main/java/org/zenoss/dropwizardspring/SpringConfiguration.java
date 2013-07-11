package org.zenoss.dropwizardspring;

import org.zenoss.dropwizardspring.eventbus.EventBusConfiguration;
import org.zenoss.dropwizardspring.websockets.WebSocketConfiguration;

public interface SpringConfiguration {
    public WebSocketConfiguration getWebSocketConfiguration();

    public EventBusConfiguration getEventBusConfiguration();
}
