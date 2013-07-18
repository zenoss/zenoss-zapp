package org.zenoss.dropwizardspring.websockets;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebSocketConfiguration {
    @JsonProperty
    private int maxBroadcastThreads = 10;

    @JsonProperty
    private int minBroadcastThreads = 5;

    @JsonProperty
    private int broadcastThreadKeepAliveMillis = 60000;

    public int getMaxBroadcastThreads() {
        return maxBroadcastThreads;
    }

    public void setMaxBroadcastThreads(int maxBroadcastThreads) {
        this.maxBroadcastThreads = maxBroadcastThreads;
    }

    public int getMinBroadcastThreads() {
        return minBroadcastThreads;
    }

    public void setMinBroadcastThreads(int minBroadcastThreads) {
        this.minBroadcastThreads = minBroadcastThreads;
    }

    public int getBroadcastThreadKeepAliveMillis() {
        return broadcastThreadKeepAliveMillis;
    }

    public void setBroadcastThreadKeepAliveMillis(int broadcastThreadKeepAliveMillis) {
        this.broadcastThreadKeepAliveMillis = broadcastThreadKeepAliveMillis;
    }
}
