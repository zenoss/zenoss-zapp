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

package org.zenoss.dropwizardspring.websockets;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebSocketConfiguration {
    /**
     * executor service maximum threads for broadcasting messages
     */
    @JsonProperty
    private int maxBroadcastThreads = 10;

    /**
     * executor service minumum threads for broadcasting messages
     */
    @JsonProperty
    private int minBroadcastThreads = 5;

    /**
     * executor service threads keep alive time
     */
    @JsonProperty
    private int broadcastThreadKeepAliveMillis = 60000;

    /**
     * used to set the time in ms that a websocket may be idle before closing.
     */
    @JsonProperty
    private Integer maxIdleTime;

    /**
     * used to set the size in bytes that a websocket may be accept before closing.
     */
    @JsonProperty
    private Integer maxBinaryMessageSize;

    /**
     * used to set the size in characters that a websocket may be accept before closing.
     */
    @JsonProperty
    private Integer maxTextMessageSize;

    /**
     * used to set the buffer size, which is also the max frame byte size (default 8192).
     */
    @JsonProperty
    private Integer bufferSize;

    /**
     * used to set the minimum protocol version accepted. Default is the RFC6455 version (13)
     */
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
