package org.zenoss.dropwizardspring.eventbus;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventBusConfiguration {
    @JsonProperty
    private int maxEventBusThreads = 10;

    @JsonProperty
    private int minEventBusThreads = 5;

    @JsonProperty
    private int broadcastThreadKeepAliveMillis = 60000;

    public int getMaxEventBusThreads() {
        return maxEventBusThreads;
    }

    public void setMaxEventBusThreads(int maxBroadcastThreads) {
        this.maxEventBusThreads = maxBroadcastThreads;
    }

    public int getMinEventBusThreads() {
        return minEventBusThreads;
    }

    public void setMinEventBusThreads(int minBroadcastThreads) {
        this.minEventBusThreads = minBroadcastThreads;
    }

    public int getEventBusThreadKeepAliveMillis() {
        return broadcastThreadKeepAliveMillis;
    }

    public void setEventBusThreadKeepAliveMillis(int broadcastThreadKeepAliveMillis) {
        this.broadcastThreadKeepAliveMillis = broadcastThreadKeepAliveMillis;
    }
}
