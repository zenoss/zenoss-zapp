package org.zenoss.dropwizardspring.websockets;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebSocketConfiguration {
    @JsonProperty
    private int maxBroadcastThreads = 10;

    @JsonProperty
    private int minBroadcastThreads = 5;

    @JsonProperty
    private int broadcastThreadKeepAliveMillis = 60000;

    @JsonProperty
    private Integer maxIdleTime;

    @JsonProperty
    private Integer maxBinaryMessageSize;

    @JsonProperty
    private Integer maxTextMessageSize;

    @JsonProperty
    private Integer bufferSize;

    @JsonProperty
    private String minVersion;


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

    public Integer getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(Integer maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public Integer getMaxBinaryMessageSize() {
        return maxBinaryMessageSize;
    }

    public void setMaxBinaryMessageSize(Integer maxBinaryMessageSize) {
        this.maxBinaryMessageSize = maxBinaryMessageSize;
    }

    public Integer getMaxTextMessageSize() {
        return maxTextMessageSize;
    }

    public void setMaxTextMessageSize(Integer maxTextMessageSize) {
        this.maxTextMessageSize = maxTextMessageSize;
    }

    public Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    public String getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }

}
